#!/usr/bin/env python3
"""
Event Success Predictor - Machine Learning Model
Prédit le succès d'un événement basé sur ses caractéristiques
"""

import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split, cross_val_score
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.tree import DecisionTreeClassifier
from sklearn.preprocessing import StandardScaler, LabelEncoder
from sklearn.metrics import accuracy_score, classification_report, confusion_matrix
import mysql.connector
import pickle
import warnings
warnings.filterwarnings('ignore')

class EventSuccessPredictor:
    def __init__(self):
        self.scaler = StandardScaler()
        self.label_encoder = LabelEncoder()
        self.model = None
        self.feature_columns = []
        
    def load_data_from_mysql(self):
        """Charge les données depuis MySQL"""
        try:
            # Connexion à la base de données
            conn = mysql.connector.connect(
                host='localhost',
                user='root',
                password='',
                database='my_crediguard'
            )
            
            # Charger les données préparées
            query = """
            SELECT 
                budget_estimated, capacity, ticket_price,
                equipment_cost, marketing_cost, staff_cost, venue_cost,
                budget_per_person, ticket_price_ratio, days_duration,
                profit_margin_estimate, event_type, season,
                actual_rating, actual_recommendation_rate, total_feedbacks, is_success
            FROM ml_event_data 
            WHERE actual_rating > 0  -- Seulement les événements avec feedback
            """
            
            df = pd.read_sql(query, conn)
            conn.close()
            
            print(f"✅ Données chargées: {len(df)} événements avec feedback")
            return df
            
        except Exception as e:
            print(f"❌ Erreur de connexion: {e}")
            # Générer des données simulées pour démonstration
            return self.generate_sample_data()
    
    def generate_sample_data(self):
        """Génère des données d'exemple si la BDD n'est pas disponible"""
        print("📊 Génération de données d'exemple...")
        np.random.seed(42)
        
        n_samples = 50
        data = {
            'budget_estimated': np.random.uniform(1000, 50000, n_samples),
            'capacity': np.random.randint(20, 500, n_samples),
            'ticket_price': np.random.uniform(10, 200, n_samples),
            'equipment_cost': np.random.uniform(100, 10000, n_samples),
            'marketing_cost': np.random.uniform(200, 8000, n_samples),
            'staff_cost': np.random.uniform(500, 15000, n_samples),
            'venue_cost': np.random.uniform(300, 12000, n_samples),
            'budget_per_person': np.random.uniform(20, 500, n_samples),
            'ticket_price_ratio': np.random.uniform(0.1, 2.0, n_samples),
            'days_duration': np.random.randint(1, 7, n_samples),
            'profit_margin_estimate': np.random.uniform(-50, 200, n_samples),
            'event_type': np.random.choice(['conference', 'workshop', 'seminar', 'networking'], n_samples),
            'season': np.random.choice(['printemps', 'été', 'automne', 'hiver'], n_samples),
            'actual_rating': np.random.uniform(2.0, 5.0, n_samples),
            'actual_recommendation_rate': np.random.uniform(20, 100, n_samples),
            'total_feedbacks': np.random.randint(5, 100, n_samples)
        }
        
        df = pd.DataFrame(data)
        
        # Calculer la target (succès)
        df['is_success'] = ((df['actual_rating'] >= 4.0) & 
                          (df['actual_recommendation_rate'] >= 80.0)).astype(int)
        
        return df
    
    def preprocess_data(self, df):
        """Prétraite les données pour le ML"""
        print("🔧 Prétraitement des données...")
        
        # Features numériques
        numeric_features = [
            'budget_estimated', 'capacity', 'ticket_price',
            'equipment_cost', 'marketing_cost', 'staff_cost', 'venue_cost',
            'budget_per_person', 'ticket_price_ratio', 'days_duration',
            'profit_margin_estimate'
        ]
        
        # Features catégorielles
        categorical_features = ['event_type', 'season']
        
        # Copier les features
        X = df[numeric_features + categorical_features].copy()
        y = df['is_success']
        
        # Encoder les variables catégorielles
        for col in categorical_features:
            X[col] = self.label_encoder.fit_transform(X[col].astype(str))
        
        # Standardiser les features numériques
        X[numeric_features] = self.scaler.fit_transform(X[numeric_features])
        
        self.feature_columns = X.columns.tolist()
        
        print(f"✅ Features préparées: {len(self.feature_columns)} variables")
        print(f"📈 Distribution du succès: {y.value_counts().to_dict()}")
        
        return X, y
    
    def train_model(self, X, y):
        """Entraîne plusieurs modèles et choisit le meilleur"""
        print("🤖 Entraînement des modèles...")
        
        # Diviser les données (adapter pour très petits datasets)
        if len(X) < 5:
            # Dataset très petit - utiliser validation croisée sur toutes les données
            print("⚠️ Dataset très petit - utilisation de validation croisée sur toutes les données")
            X_train, X_test, y_train, y_test = train_test_split(
                X, y, test_size=0.3, random_state=42, shuffle=True
            )
            # Si le test set n'a qu'une seule classe, utiliser tout pour l'entraînement
            unique_classes = set(y_test)
            if len(unique_classes) < 2:
                print("⚠️ Test set avec une seule classe - entraînement sur toutes les données")
                X_train, X_test, y_train, y_test = X, X[:1], y, y[:1]
        else:
            # Dataset suffisant - utiliser stratification
            X_train, X_test, y_train, y_test = train_test_split(
                X, y, test_size=0.3, random_state=42, stratify=y
            )
        
        # Tester différents modèles
        models = {
            'Logistic Regression': LogisticRegression(random_state=42),
            'Decision Tree': DecisionTreeClassifier(random_state=42, max_depth=3),
            'Random Forest': RandomForestClassifier(n_estimators=10, random_state=42, max_depth=3)
        }
        
        best_model = None
        best_score = 0
        best_name = ""
        
        for name, model in models.items():
            if len(X_train) < 5:
                # Dataset très petit - entraînement simple sans validation croisée
                print(f"⚠️ {name}: entraînement simple (dataset trop petit)")
                model.fit(X_train, y_train)
                score = model.score(X_test, y_test)
                mean_score = score
                std_score = 0
            else:
                # Dataset suffisant - validation croisée adaptée
                cv_folds = min(5, len(X_train))
                scores = cross_val_score(model, X_train, y_train, cv=cv_folds, scoring='accuracy')
                mean_score = scores.mean()
                std_score = scores.std()
            
            print(f"📊 {name}: {mean_score:.3f} (+/- {std_score * 2:.3f})")
            
            if mean_score > best_score:
                best_score = mean_score
                best_model = model
                best_name = name
        
        # Entraîner le meilleur modèle
        best_model.fit(X_train, y_train)
        
        # Évaluer sur le test set
        y_pred = best_model.predict(X_test)
        accuracy = accuracy_score(y_test, y_pred)
        
        print(f"\n🏆 Meilleur modèle: {best_name}")
        print(f"📈 Accuracy test: {accuracy:.3f}")
        print("\n📋 Rapport de classification:")
        print(classification_report(y_test, y_pred))
        
        self.model = best_model
        
        # Feature importance
        if hasattr(best_model, 'feature_importances_'):
            self.show_feature_importance()
    
    def show_feature_importance(self):
        """Affiche l'importance des features"""
        importances = pd.DataFrame({
            'feature': self.feature_columns,
            'importance': self.model.feature_importances_
        }).sort_values('importance', ascending=False)
        
        print("\n🎯 Importance des features:")
        for _, row in importances.head(10).iterrows():
            print(f"  {row['feature']}: {row['importance']:.3f}")
    
    def predict_event_success(self, event_data):
        """Prédit le succès d'un nouvel événement"""
        if self.model is None:
            print("❌ Modèle non entraîné")
            return None
        
        # Préparer les données
        df = pd.DataFrame([event_data])
        
        # Simplifier: utiliser des valeurs numériques par défaut pour les catégorielles
        # Pour éviter les problèmes d'encodage, nous utilisons des mappages simples
        if 'event_type' in df.columns:
            event_type_mapping = {
                'conference': 1.0,
                'workshop': 2.0,
                'seminar': 3.0,
                'networking': 4.0
            }
            event_type_str = str(df['event_type'].iloc[0]).lower()
            df['event_type'] = event_type_mapping.get(event_type_str, 1.0)
            print(f"📝 event_type mappé: {event_type_str} -> {df['event_type'].iloc[0]}")
        
        if 'season' in df.columns:
            season_mapping = {
                'printemps': 1.0,
                'été': 2.0,
                'automne': 3.0,
                'hiver': 4.0
            }
            season_str = str(df['season'].iloc[0]).lower()
            df['season'] = season_mapping.get(season_str, 1.0)
            print(f"📝 season mappé: {season_str} -> {df['season'].iloc[0]}")
        
        # Standardiser
        numeric_features = [col for col in self.feature_columns if col not in ['event_type', 'season']]
        df[numeric_features] = self.scaler.transform(df[numeric_features])
        
        # Prédire - s'assurer que les colonnes sont dans le bon ordre
        df_ordered = df[self.feature_columns]
        
        # Débogage: vérifier les types
        print(f"🔍 Types des features:")
        for col in df_ordered.columns:
            print(f"  {col}: {df_ordered[col].dtype} - {type(df_ordered[col].iloc[0])}")
        
        # Convertir en numpy array avec types corrects
        X_pred = df_ordered.values.astype(float)
        print(f"🔍 Shape final: {X_pred.shape}, Type: {X_pred.dtype}")
        
        prediction = self.model.predict(X_pred)[0]
        probability = self.model.predict_proba(X_pred)[0]
        
        return {
            'success': bool(prediction),
            'confidence': probability[prediction] * 100,
            'failure_probability': probability[0] * 100
        }
    
    def save_model(self, filename='event_success_model.pkl'):
        """Sauvegarde le modèle entraîné"""
        model_data = {
            'model': self.model,
            'scaler': self.scaler,
            'label_encoder': self.label_encoder,
            'feature_columns': self.feature_columns
        }
        
        with open(filename, 'wb') as f:
            pickle.dump(model_data, f)
        
        print(f"💾 Modèle sauvegardé: {filename}")
    
    def load_model(self, filename='event_success_model.pkl'):
        """Charge un modèle entraîné"""
        try:
            with open(filename, 'rb') as f:
                model_data = pickle.load(f)
            
            self.model = model_data['model']
            self.scaler = model_data['scaler']
            self.label_encoder = model_data['label_encoder']
            self.feature_columns = model_data['feature_columns']
            
            print(f"✅ Modèle chargé: {filename}")
            return True
        except FileNotFoundError:
            print(f"❌ Fichier {filename} non trouvé")
            return False

def main():
    """Fonction principale"""
    predictor = EventSuccessPredictor()
    
    # Charger et préparer les données
    df = predictor.load_data_from_mysql()
    X, y = predictor.preprocess_data(df)
    
    # Entraîner le modèle
    predictor.train_model(X, y)
    
    # Sauvegarder le modèle
    predictor.save_model()
    
    # Exemple de prédiction
    sample_event = {
        'budget_estimated': 10000,
        'capacity': 100,
        'ticket_price': 50,
        'equipment_cost': 2000,
        'marketing_cost': 1500,
        'staff_cost': 3000,
        'venue_cost': 2500,
        'budget_per_person': 100,
        'ticket_price_ratio': 0.5,
        'days_duration': 2,
        'profit_margin_estimate': 25.0,
        'event_type': 'conference',
        'season': 'printemps'
    }
    
    result = predictor.predict_event_success(sample_event)
    print(f"\n🔮 Prédiction pour l'événement exemple:")
    print(f"   Succès: {result['success']}")
    print(f"   Confiance: {result['confidence']:.1f}%")

if __name__ == "__main__":
    main()
