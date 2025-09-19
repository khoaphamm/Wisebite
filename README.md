# 🍽️ WiseBite - Food Waste Reduction Platform

WiseBite is a comprehensive platform that connects customers with local vendors offering discounted surprise bags of food items, helping reduce food waste while providing affordable meal options.

## 🌟 Features

- **Surprise Bags**: Vendors can create mystery food bags at discounted prices
- **Location-Based Search**: Find nearby participating stores using GPS
- **Real-Time Orders**: Instant order processing and notifications
- **User Profiles**: Separate customer and vendor account types
- **Secure Payments**: JWT-based authentication and secure transactions
- **Review System**: Rate and review surprise bags and stores
- **Chat System**: Direct communication between customers and vendors

## 🏗️ Project Structure

```
Wisebite/
├── backend/                 # FastAPI backend server
│   ├── app/                # Main application code
│   ├── tests/              # Test suite
│   ├── docker-compose.yml  # Docker configuration
│   └── README.md           # Backend documentation
└── README.md               # This file
```

## 🚀 Quick Start

### Prerequisites

- Python 3.12+
- PostgreSQL 15+
- Docker & Docker Compose (recommended)

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/wisebite.git
cd wisebite
```

### 2. Backend Setup

```bash
cd backend

# Copy environment configuration
cp .env.example .env
# Edit .env with your configuration

# Start with Docker (recommended)
docker-compose up --build -d

# Or manual setup
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 3. Access the Application

- **API Documentation**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc
- **API Base URL**: http://localhost:8000/api/v1

## 🛠️ Development

### Backend Development

See [backend/README.md](backend/README.md) for detailed backend setup and API documentation.

### Testing

```bash
cd backend
# Run all tests
.\run-tests.ps1

# Run specific test types
.\run-tests.ps1 -TestType auth
pytest tests/api/endpoints/test_auth.py -v
```

### Database Schema

The application uses PostgreSQL with the following main entities:
- **Users** (customers and vendors)
- **Stores** (vendor locations)
- **Food Items** (menu items)
- **Surprise Bags** (discounted mystery bags)
- **Orders** (customer purchases)
- **Reviews** (ratings and feedback)

## 📱 API Endpoints

### Authentication
- `POST /api/v1/auth/register` - User registration
- `POST /api/v1/auth/login` - User login
- `GET /api/v1/auth/me` - Get current user

### Stores & Surprise Bags
- `GET /api/v1/stores/` - List stores
- `GET /api/v1/surprise-bags/` - List available surprise bags
- `POST /api/v1/surprise-bags/` - Create surprise bag (vendors)

### Orders
- `POST /api/v1/orders/` - Create order
- `GET /api/v1/orders/my-orders` - Get user's orders

For complete API documentation, visit http://localhost:8000/docs after starting the server.

## 🔧 Technologies Used

### Backend
- **FastAPI** - Modern Python web framework
- **SQLModel** - SQL database ORM with type annotations
- **PostgreSQL** - Primary database with PostGIS for location data
- **JWT** - Authentication and authorization
- **Docker** - Containerization and deployment
- **Pytest** - Testing framework

### External Services
- **Cloudinary** - Image storage and management
- **Mapbox** - Location services and mapping

## 🌐 Deployment

### Docker Deployment

```bash
# Production deployment
docker-compose -f docker-compose.yml up -d

# View logs
docker-compose logs -f app
```

### Environment Configuration

Make sure to configure these environment variables for production:

```env
SECRET_KEY=your_production_secret_key
POSTGRES_PASSWORD=secure_password
CLOUDINARY_API_SECRET=your_cloudinary_secret
MAPBOX_ACCESS_TOKEN=your_mapbox_token
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Write tests for new features
- Follow PEP 8 style guidelines
- Update documentation for API changes
- Ensure all tests pass before submitting PR

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the backend README for detailed setup instructions
- **Issues**: Open an issue on GitHub for bugs or feature requests
- **API Questions**: Use the interactive API docs at `/docs` endpoint

## 🎯 Roadmap

- [ ] Mobile app development (React Native)
- [ ] Advanced analytics dashboard for vendors
- [ ] Push notifications
- [ ] Integration with more payment providers
- [ ] Multi-language support
- [ ] Admin panel for platform management

## 📞 Contact

For questions or collaboration opportunities, please open an issue or contact the development team.

---

**Made with ❤️ to reduce food waste and help communities**
