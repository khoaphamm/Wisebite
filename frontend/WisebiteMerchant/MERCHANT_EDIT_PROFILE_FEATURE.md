# Merchant Edit Profile Feature

## Overview
This feature allows merchants to edit both their personal information and store information in a single, comprehensive interface. The screen is designed to match the existing customer EditProfileScreen pattern but is tailored specifically for merchant needs.

## Files Created/Modified

### New Files
1. **MerchantEditProfileViewModel.kt** - Handles state management for both personal and store data
2. **MerchantEditProfileScreen.kt** - UI for editing merchant and store information
3. **StoreRepository.kt** - Repository for store-related API operations

### Modified Files
1. **MerchantApiService.kt** - Added updateProfile() and updateStore() endpoints
2. **AuthRepository.kt** - Added updateProfile() method
3. **ViewModelFactory.kt** - Added StoreRepository and MerchantEditProfileViewModel support
4. **Routes.kt** - Added EDIT_PROFILE route
5. **MainScreen.kt** - Added navigation to edit profile screen

## Features

### Personal Information Section
- **Profile Image**: Clickable circular image with camera icon overlay
- **Full Name**: Required field for merchant's full name
- **Email**: Required field with email validation
- **Phone Number**: Required field with phone validation

### Store Information Section
- **Store Image**: Clickable circular image with camera icon overlay
- **Store Name**: Required field for store display name
- **Store Description**: Optional multi-line description
- **Store Address**: Required multi-line address field
- **Business Hours**: Optional field (e.g., "8:00 - 22:00")
- **Store Phone**: Optional separate phone number for the store

## Design Features

### Expandable Sections
- Both personal and store information sections can be collapsed/expanded
- Orange theme for personal info section
- Blue theme for store info section
- Smooth animations for expand/collapse

### Smart Save Functionality
- Save button only enabled when there are actual changes
- Visual indication of save state (loading spinner during save)
- Success/error message display
- Automatic data refresh after successful save

### Validation
- Real-time validation for required fields
- Email format validation
- Phone number length validation
- Clear error messages in Vietnamese

### User Experience
- Auto-scroll to top when success/error messages appear
- Consistent spacing and padding throughout
- Material Design 3 theming
- Responsive layout for different screen sizes

## Navigation

### Access Path
1. Main Screen → Profile Tab → Edit Profile button
2. Direct navigation via `Routes.EDIT_PROFILE`

### Integration
- Integrated with existing ProfileScreen via `onNavigateToEditProfile` callback
- Uses consistent navigation patterns with the rest of the app
- Proper back navigation support

## API Integration

### Endpoints Used
- `PUT /user/me` - Update personal information
- `PUT /stores/me` - Update store information
- `GET /user/me` - Get current user data
- `GET /stores/me` - Get current store data

### Error Handling
- Network connectivity errors
- Authentication/authorization errors
- Validation errors from backend
- User-friendly error messages in Vietnamese

## Technical Implementation

### Architecture
- MVVM pattern with StateFlow for reactive UI
- Repository pattern for data access
- Singleton repositories with proper DI
- Separation of concerns between personal and store data

### State Management
- Single ViewModel managing both personal and store data
- Reactive UI updates with StateFlow
- Proper loading states and error handling
- Change detection for optimized save operations

### Memory Management
- Efficient image loading (placeholder implementation ready for actual image loading)
- Proper ViewModel lifecycle management
- Coroutine scope management for async operations

## Future Enhancements

### Image Upload
- Currently shows placeholder icons
- Ready for integration with image picker and upload service
- Camera icon overlay prepared for image selection

### Validation Enhancement
- Can add more sophisticated validation rules
- Real-time field validation as user types
- Custom validation messages per field

### Additional Fields
- Easy to add more personal or store fields
- Modular design supports field additions
- Consistent styling for new fields

## Usage Example

```kotlin
// In navigation setup
composable(Routes.EDIT_PROFILE) {
    MerchantEditProfileScreen(
        onNavigateBack = {
            navController.popBackStack()
        },
        onNavigateToImagePicker = { isStoreImage ->
            // Navigate to image picker
            // isStoreImage = true for store image, false for profile image
        }
    )
}
```

## Customization

### Colors
- Orange theme for personal info (Orange50, Orange500, Orange600, etc.)
- Blue theme for store info (Blue100, Blue500, Blue600, etc.)
- Error colors using Red variants
- Success colors using Green variants

### Text
- All text is in Vietnamese
- Consistent with existing app language
- Required fields marked with "*"
- Placeholder text for guidance

This implementation provides a comprehensive, user-friendly interface for merchants to manage both their personal information and store details in one cohesive experience.