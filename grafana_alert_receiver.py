from fastapi import FastAPI, Request
import uvicorn

app = FastAPI()

@app.post("/alerts/grafana")
async def receive_alert(req: Request):
    data = await req.json()
    print("\n🔥 Grafana Alert Received 🔥")
    print(data)
    return {"ok": True}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8085)
