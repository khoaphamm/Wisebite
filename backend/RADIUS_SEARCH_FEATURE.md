# Store Radius Search Feature Implementation

## Overview
This feature allows vendors to view all stores within a specified radius from their location and sort them by proximity. The implementation uses PostGIS for spatial operations and integrates seamlessly with the existing Wisebite backend architecture.

## API Endpoints

### 1. GET /api/v1/stores/nearby
Retrieve stores within a specified radius from a given location, sorted by distance.

**Parameters:**
- `latitude` (required): Latitude of the search location (-90 to 90)
- `longitude` (required): Longitude of the search location (-180 to 180)  
- `radius` (required): Search radius in kilometers (0.1 to 100.0)
- `skip` (optional): Number of records to skip for pagination (default: 0)
- `limit` (optional): Maximum number of records to return (default: 100, max: 100)

**Response Format:**
```json
[
  {
    "id": "uuid",
    "name": "Store Name",
    "address": "Store Address",
    "description": "Store Description",
    "logo_url": "https://...",
    "owner_id": "uuid",
    "latitude": 10.7769,
    "longitude": 106.7009,
    "distance_km": 2.45
  }
]
```

**Example Request:**
```bash
GET /api/v1/stores/nearby?latitude=10.7769&longitude=106.7009&radius=5.0&limit=20
```

### 2. Enhanced Store Creation/Update
Store creation and update endpoints now accept optional latitude and longitude coordinates.

**Additional Fields:**
- `latitude` (optional): Store latitude coordinate
- `longitude` (optional): Store longitude coordinate

## Database Schema Changes

### Store Model Enhancement
The existing `Store` model already includes a PostGIS `location` field:
```python
location: Optional[Any] = Field(
    sa_column=Column(Geometry(geometry_type="POINT", srid=4326), nullable=True), 
    default=None
)
```

No database migrations are required as the location field already exists.

## Implementation Details

### Schemas (app/schemas/store.py)
- `StoreCreate`: Added optional `latitude` and `longitude` fields
- `StoreUpdate`: Added optional `latitude` and `longitude` fields  
- `StorePublic`: Added optional `latitude` and `longitude` fields for coordinate display
- `StoreWithDistance`: New schema extending `StorePublic` with `distance_km` field

### CRUD Operations (app/crud.py)
- `create_store()`: Enhanced to handle coordinate data and create PostGIS POINT geometry
- `update_store()`: Enhanced to update location coordinates
- `get_stores_within_radius()`: New function implementing spatial query with distance sorting
- `_extract_coordinates_from_store()`: Helper function to extract lat/lon from PostGIS geometry

### Spatial Query Implementation
Uses PostGIS functions for accurate distance calculations:
- `ST_Distance_Sphere()`: Calculates spherical distance between points
- `WKTElement()`: Creates PostGIS geometry from WKT (Well-Known Text) format
- `ST_X()` and `ST_Y()`: Extract longitude and latitude from geometry

### API Endpoints (app/api/endpoints/store.py)
- Enhanced existing endpoints to support location data
- Added new `/nearby` endpoint with parameter validation
- Comprehensive input validation for coordinates and radius values

## Testing

### Test Coverage
- Unit tests for schema validation
- Integration tests for radius-based filtering
- Parameter validation tests for invalid inputs
- Location coordinate handling tests

### Key Test Cases
- Valid radius search with various distances
- Invalid parameter rejection (negative radius, out-of-range coordinates)
- Missing parameter handling
- Store creation/update with location data
- Distance calculation accuracy

## Performance Considerations

### Spatial Indexing
The PostGIS location field should have a spatial index for optimal performance:
```sql
CREATE INDEX idx_store_location ON store USING GIST (location);
```

### Query Optimization
- Uses `ST_Distance_Sphere()` for accurate Earth surface distance calculation
- Filters by radius first, then sorts by distance
- Supports pagination to limit result set size
- Configurable radius limits prevent excessive query scope

## Integration with Existing Features

### Mapbox Service Integration
The implementation is designed to work with the existing Mapbox service (`app/services/mapbox.py`) for:
- Address geocoding to obtain coordinates
- Route calculation for delivery planning
- Travel time estimation

### Frontend Integration
The API provides all necessary data for frontend map displays:
- Store coordinates for map markers
- Distance information for sorting/filtering
- Pagination support for large result sets

## Configuration

### Environment Variables
No additional environment variables required. Uses existing PostGIS database connection.

### Default Values
- Default search radius: 10.0 km
- Maximum search radius: 100.0 km
- Minimum search radius: 0.1 km
- Default pagination limit: 100 stores

## Error Handling

### Validation Errors (422 status)
- Invalid latitude/longitude values
- Radius outside allowed range (0.1-100 km)
- Missing required parameters

### Database Errors (500 status)  
- PostGIS extension not available
- Invalid geometry data
- Database connection issues

## Usage Examples

### Basic Radius Search
```python
# Find stores within 5 km of Ho Chi Minh City center
GET /api/v1/stores/nearby?latitude=10.7769&longitude=106.7009&radius=5.0
```

### Pagination
```python
# Get next page of results
GET /api/v1/stores/nearby?latitude=10.7769&longitude=106.7009&radius=5.0&skip=20&limit=20
```

### Store Creation with Location
```python
POST /api/v1/stores/
{
  "name": "My Food Store",
  "address": "123 Main St, Ho Chi Minh City",
  "description": "Fresh local food",
  "latitude": 10.7769,
  "longitude": 106.7009
}
```

## Future Enhancements

### Potential Improvements
- Real-time distance updates based on vendor location changes
- Integration with device GPS for automatic location detection
- Caching of frequent radius searches
- Advanced filtering by store category within radius
- Travel time estimation using Mapbox routing

### Performance Optimizations
- Spatial clustering for large datasets
- Background pre-calculation of popular search areas
- Redis caching for common radius queries
- Connection pooling for concurrent spatial queries

## Deployment Notes

### Database Requirements
- PostgreSQL with PostGIS extension
- Spatial indexes on location fields
- Adequate connection pool size for spatial queries

### Monitoring
- Query performance monitoring for spatial operations
- API response time tracking for radius searches
- Error rate monitoring for coordinate validation

This implementation provides a solid foundation for location-based store discovery while maintaining compatibility with the existing Wisebite architecture.