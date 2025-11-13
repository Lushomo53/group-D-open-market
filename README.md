# 🛒 Open Market Tracker

Open Market Tracker is a mobile application designed to help users monitor and compare market commodity prices across different vendors and regions. The app visualizes **price trends**, allows users to **add or update commodity prices**, and helps make **data-driven buying or selling decisions**.

---

## 🚀 Features

- 📈 **Price Trend Visualization** — View price changes over time through interactive graphs.
- 🏷️ **Commodity List** — Browse all available commodities on the market.
- ✏️ **Update Commodity Prices** — Contribute by updating the current price of any listed item.
- ➕ **Add New Commodities** — Add newly introduced items to the system.
- 🔄 **Real-Time Sync** — Prices are fetched dynamically from the Django backend.
- 🌐 **Network Integration** — Works seamlessly with a locally or remotely hosted backend via Retrofit.

---

## 🧰 Tech Stack

### Frontend (Android)
- **Language:** Java  
- **Networking:** Retrofit2 + GSON  
- **UI Framework:** Android SDK (XML-based layouts)  
- **Charting Library:** MPAndroidChart (for trend graphs)

### Backend
- **Framework:** Django + Django REST Framework (DRF)  
- **Database:** mySQL / SQLite  
- **Hosting:** Can be run locally or deployed on a VPS  
- **API Communication:** REST (JSON-based)

---

## ⚙️ Project Setup

### 1️⃣ Backend (Django)

#### 🧩 Requirements
- Python 3.9+
- Django 3.7
- Django REST Framework
- mySQL (or SQLite for local dev)
- CORS Headers

#### 📦 Installation
```bash
# Clone the repository
git clone https://github.com/lushomo53/open-market-tracker-backend.git
cd open-market-tracker-backend

# Create and activate a virtual environment
python -m venv venv
source venv/bin/activate  # on Windows: venv\Scripts\activate

# Install dependencies
pip install -r requirements.txt

# Run migrations
python manage.py migrate

# Start the server
python manage.py runserver 0.0.0.0:8000