from fastapi import FastAPI
from pydantic import BaseModel
from rag import create_chain
import uvicorn

app = FastAPI(title="CrediGuard Insurance RAG API")

# Initialize the chain
try:
    qa_chain = create_chain()
except Exception as e:
    print(f"Error initializing chain: {e}")
    qa_chain = None

class Question(BaseModel):
    query: str

@app.get("/")
def root():
    return {
        "service": "CrediGuard Insurance Advisor",
        "status": "Ready" if qa_chain else "Error (Check index/data)",
        "docs": "/docs"
    }

@app.post("/ask")
def ask(q: Question):
    if not qa_chain:
        return {"error": "RAG Chain not initialized", "answer": None}
    
    try:
        print(f"--- QUESTION REÇUE : {q.query}")
        response = qa_chain.invoke(q.query)
        print(f"--- RÉPONSE IA : {response}")
        return {"answer": str(response).strip()}
    except Exception as e:
        print(f"--- ERREUR PYTHON : {e}")
        return {"error": str(e), "answer": None}

if __name__ == "__main__":
    uvicorn.run(app, host="127.0.0.1", port=8099)
