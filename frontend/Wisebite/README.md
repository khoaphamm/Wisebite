# WiseBite Android App

The Android frontend for WiseBite - a food waste reduction platform that connects customers with vendors offering discounted surprise bags.

## 🏗️ Architecture

This app is built using:
- **Jetpack Compose** for modern UI
- **MVVM Architecture** with ViewModels
- **Retrofit** for API communication  
- **DataStore** for secure token storage
- **Navigation Compose** for screen navigation
- **Material Design 3** for theming

## 📁 Project Structure

```
app/src/main/java/com/example/wisebite/
├── data/
│   ├── model/          # Data models and DTOs
│   ├── remote/         # API services and Retrofit setup
│   └── repository/     # Repository layer for data access
├── navigation/         # Navigation setup and routes
├── ui/
│   ├── component/      # Reusable UI components
│   ├── screen/         # UI screens (Login, Signup, Home)
│   ├── theme/          # App theming (colors, typography)
│   └── viewmodel/      # ViewModels for state management
└── util/              # Utility classes and helpers
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybird | 2024.2.1 or newer
- Android SDK 24 (Android 7.0) or higher
- Kotlin support enabled

### Setup Instructions

1. **Open the Project**
   ```bash
   # Open the frontend/Wisebite folder in Android Studio
   cd frontend/Wisebite
   ```

2. **Sync Dependencies**
   - Android Studio will automatically prompt to sync Gradle files
   - If not, click "Sync Now" in the notification bar

3. **Configure Backend Connection**
   
   The app is configured to connect to your backend API. Make sure your backend is running:
   
   **For Android Emulator:**
   - API Base URL is set to `http://10.0.2.2:8000/api/v1/`
   - This maps to `localhost:8000` on your development machine
   
   **For Physical Device:**
   - Change the BASE_URL in `WisebiteApiService.kt` to your computer's IP address
   - Example: `http://192.168.1.100:8000/api/v1/`

4. **Start Your Backend Server**
   ```bash
   # In the backend directory
   cd ../../backend
   docker-compose up -d
   # Or run manually: uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
   ```

5. **Run the App**
   - Click the "Run" button in Android Studio
   - Or use the keyboard shortcut: `Shift + F10`

## 📱 Features Implemented

### ✅ Authentication
- **User Registration** - Create customer accounts
- **User Login** - Authenticate with phone number and password
- **JWT Token Management** - Secure token storage with DataStore
- **Form Validation** - Real-time validation with error messages
- **Password Visibility Toggle** - Show/hide password functionality

### ✅ UI Components
- **Custom Text Fields** - Styled input fields with error states
- **Gradient Buttons** - Attractive call-to-action buttons
- **Material Design 3** - Modern theming with food-inspired colors
- **Responsive Layout** - Works on different screen sizes
- **Loading States** - Visual feedback during API calls

### ✅ Navigation
- **Screen Navigation** - Seamless flow between Login, Signup, and Home
- **Back Navigation** - Proper back button handling
- **Deep Linking Ready** - Structure supports future deep linking

## 🎨 Design System

### Color Palette
- **Primary**: Orange (#FF9800) - Food/appetite inspiring
- **Secondary**: Green (#4CAF50) - Fresh/sustainable theme  
- **Background**: Dynamic based on system theme
- **Error**: Red for validation errors

### Typography
- Material Design 3 typography scale
- Custom font weights for different text importance

## 🔧 Configuration

### API Configuration

Update the base URL in `WisebiteApiService.kt` for different environments:

```kotlin
companion object {
    const val BASE_URL = "http://10.0.2.2:8000/api/v1/" // Android Emulator
    // const val BASE_URL = "http://192.168.1.100:8000/api/v1/" // Physical Device
    // const val BASE_URL = "https://api.wisebite.com/api/v1/" // Production
}
```

### Network Security

The app includes:
- HTTPS support for production
- Clear text traffic allowed for development
- Network security configuration ready

## 🧪 Testing

### Manual Testing Steps

1. **Test Registration**:
   - Open the app → Click "Sign up"
   - Fill all required fields with valid data
   - Verify validation messages for invalid inputs
   - Check successful registration

2. **Test Login**:
   - Use registered phone number and password
   - Verify error messages for wrong credentials
   - Check successful login navigation

3. **Test API Connection**:
   - Ensure backend is running on port 8000
   - Check Android logs for network requests
   - Verify JWT token storage

### Debug Tools

- **Network Logging**: Retrofit logs all API requests/responses
- **Logcat**: View detailed logs in Android Studio
- **Database Inspector**: Inspect DataStore preferences

## 🚧 Upcoming Features

The app structure is ready for implementing:

- **Browse Stores** - View nearby vendor locations
- **Surprise Bags** - Browse available discounted food packages
- **Order Management** - Place and track orders
- **User Profile** - Edit profile and view order history
- **Chat System** - Communicate with vendors
- **Push Notifications** - Order updates and promotions
- **Location Services** - GPS-based store discovery
- **Payment Integration** - Secure payment processing

## 🔗 API Integration

The app integrates with the following backend endpoints:

### Authentication
- `POST /auth/signup` - User registration
- `POST /auth/login` - User authentication
- `GET /users/me` - Get current user profile

### Data Models

All API data models are defined in `data/model/User.kt` and follow the backend API schema exactly.

## 🛠️ Development Notes

### State Management
- ViewModels handle UI state and business logic
- StateFlow for reactive UI updates
- Repository pattern for data access

### Error Handling
- Network errors are caught and display user-friendly messages
- Form validation with real-time feedback
- API error responses properly parsed and displayed

### Performance
- Compose for efficient UI rendering
- Coroutines for asynchronous operations
- Retrofit for optimized networking

## 📞 Support

If you encounter issues:

1. **Check Backend Connection**: Ensure your API server is running and accessible
2. **Verify Dependencies**: Run "Clean Project" and "Rebuild Project" in Android Studio
3. **Check Logs**: Look at Logcat for detailed error messages
4. **Network Issues**: Verify internet permissions and network configuration

---

**Built with ❤️ using Jetpack Compose and Modern Android Architecture**