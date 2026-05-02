from langchain_community.vectorstores import FAISS
from langchain_community.embeddings import HuggingFaceEmbeddings
from langchain_community.document_loaders import TextLoader
from langchain_core.runnables import RunnablePassthrough
from langchain_core.prompts import ChatPromptTemplate
from langchain_groq import ChatGroq
from langchain_core.output_parsers import StrOutputParser

import os
from pathlib import Path

# ⚠️ METTEZ VOTRE CLÉ GROQ ICI (console.groq.com)
groq_api_key = os.getenv("GROQ_API_KEY")
def create_chain():
    base_path = Path(__file__).parent
    data_dir = base_path / "data"
    index_path = base_path / "faiss_index"
    
    documents = []
    if data_dir.exists():
        for txt_file in data_dir.glob("*.txt"):
            loader = TextLoader(str(txt_file), encoding='utf-8')
            documents.extend(loader.load())

    embeddings = HuggingFaceEmbeddings(model_name="sentence-transformers/all-MiniLM-L6-v2")

    if index_path.exists():
        db = FAISS.load_local(str(index_path), embeddings, allow_dangerous_deserialization=True)
    else:
        db = FAISS.from_documents(documents, embeddings)
        db.save_local(str(index_path))

    retriever = db.as_retriever(search_kwargs={"k": 3})

    # 🔥 Utilisation de Llama-3.1 sur Groq (Vitesse éclair)
    llm = ChatGroq(
        model_name="llama-3.1-8b-instant",
        temperature=0.2
    )

    template = """Tu es l'assistant IA de CrediGuard Tunisie, plateforme d'assurance crédit et de gestion des risques.

## RÈGLES ABSOLUES
1. Réponds TOUJOURS en français, de façon professionnelle et bienveillante.
2. Base tes réponses UNIQUEMENT sur la base de connaissances fournie. N'invente rien.
3. Pour les messages courts ("merci", "ok", "bonsoir"), réponds poliment et propose ton aide.
4. Pour les questions hors-sujet, redirige vers les services CrediGuard.
5. Sois concis (3–5 phrases max) sauf si le client demande un détail approfondi.
6. Propose toujours une action concrète à la fin (simuler, souscrire, contacter un conseiller).
7. Si tu n'as pas l'information, dis-le et propose de contacter support@crediguard.tn.

## BASE DE CONNAISSANCES
{context}

## QUESTION DU CLIENT
{question}

## FORMAT DE RÉPONSE
- Commence directement par la réponse
- Utilise des listes courtes si nécessaire
- Termine par une suggestion d'action
"""

    prompt = ChatPromptTemplate.from_template(template)

    qa_chain = (
        {"context": retriever, "question": RunnablePassthrough()}
        | prompt
        | llm
        | StrOutputParser()
    )

    return qa_chain
