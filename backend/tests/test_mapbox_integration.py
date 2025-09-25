# """
# Integration test for Mapbox services with hardcoded token for testing.
# """
# import asyncio
# import pytest
# from unittest.mock import patch
# from app.services.mapbox import get_travel_info_multi_profile, get_travel_info_from_mapbox


# # Hardcode token for testing as per instructions
# HARDCODED_MAPBOX_TOKEN = "pk.test.hardcoded_for_testing_only"


# @pytest.mark.asyncio
# @patch('app.core.config.settings.MAPBOX_ACCESS_TOKEN', HARDCODED_MAPBOX_TOKEN)
# @patch('httpx.AsyncClient.get')
# async def test_get_travel_info_multi_profile_mock_success(mock_get):
#     """Test multi-profile travel info with mocked Mapbox responses."""
    
#     # Mock successful Mapbox responses for both driving and walking
#     mock_responses = [
#         # Driving response
#         type('MockResponse', (), {
#             'json': lambda: {
#                 "code": "Ok",
#                 "durations": [[900, 1200]],  # 15 min, 20 min
#                 "distances": [[5000, 7000]]  # 5km, 7km
#             },
#             'raise_for_status': lambda: None
#         })(),
#         # Walking response
#         type('MockResponse', (), {
#             'json': lambda: {
#                 "code": "Ok", 
#                 "durations": [[3600, 4800]],  # 1 hour, 1.33 hours
#                 "distances": [[4500, 6500]]   # 4.5km, 6.5km
#             },
#             'raise_for_status': lambda: None
#         })()
#     ]
    
#     mock_get.side_effect = mock_responses
    
#     origin = (106.7009, 10.7769)  # Ho Chi Minh City (lng, lat)
#     destinations = [
#         (106.7109, 10.7869),  # Destination 1
#         (106.7209, 10.7969)   # Destination 2
#     ]
    
#     result = await get_travel_info_multi_profile(origin, destinations)
    
#     # Verify structure
#     assert result is not None
#     assert "driving" in result
#     assert "walking" in result
    
#     # Verify driving data
#     assert len(result["driving"]) == 2
#     assert result["driving"][0]["duration"] == 900
#     assert result["driving"][0]["distance"] == 5000
#     assert result["driving"][1]["duration"] == 1200
#     assert result["driving"][1]["distance"] == 7000
    
#     # Verify walking data
#     assert len(result["walking"]) == 2
#     assert result["walking"][0]["duration"] == 3600
#     assert result["walking"][0]["distance"] == 4500
#     assert result["walking"][1]["duration"] == 4800
#     assert result["walking"][1]["distance"] == 6500
    
#     # Verify two API calls were made (one for each profile)
#     assert mock_get.call_count == 2


# @pytest.mark.asyncio
# @patch('app.core.config.settings.MAPBOX_ACCESS_TOKEN', HARDCODED_MAPBOX_TOKEN)
# @patch('httpx.AsyncClient.get')
# async def test_get_travel_info_from_mapbox_mock_error(mock_get):
#     """Test single profile travel info with mocked error response."""
    
#     # Mock HTTP error
#     mock_get.side_effect = Exception("Network error")
    
#     origin = (106.7009, 10.7769)
#     destinations = [(106.7109, 10.7869)]
    
#     result = await get_travel_info_from_mapbox(origin, destinations, "mapbox/driving-traffic")
    
#     # Should return None on error
#     assert result is None


# @pytest.mark.asyncio
# async def test_get_travel_info_empty_destinations():
#     """Test travel info function with empty destinations."""
    
#     origin = (106.7009, 10.7769)
#     destinations = []
    
#     # Single profile
#     result = await get_travel_info_from_mapbox(origin, destinations)
#     assert result == []
    
#     # Multi profile
#     result = await get_travel_info_multi_profile(origin, destinations)
#     assert result == {"driving": [], "walking": []}


# @pytest.mark.asyncio
# @patch('app.core.config.settings.MAPBOX_ACCESS_TOKEN', HARDCODED_MAPBOX_TOKEN)
# @patch('httpx.AsyncClient.get')
# async def test_get_travel_info_partial_failure(mock_get):
#     """Test multi-profile when one profile succeeds and another fails."""
    
#     mock_responses = [
#         # Driving succeeds
#         type('MockResponse', (), {
#             'json': lambda: {
#                 "code": "Ok",
#                 "durations": [[900]],
#                 "distances": [[5000]]
#             },
#             'raise_for_status': lambda: None
#         })(),
#         # Walking fails
#         Exception("Walking API error")
#     ]
    
#     mock_get.side_effect = mock_responses
    
#     origin = (106.7009, 10.7769)
#     destinations = [(106.7109, 10.7869)]
    
#     result = await get_travel_info_multi_profile(origin, destinations) 
    
#     # Should have driving data and empty walking data
#     assert result is not None
#     assert "driving" in result
#     assert "walking" in result
#     assert len(result["driving"]) == 1
#     assert result["driving"][0]["duration"] == 900
#     assert len(result["walking"]) == 0  # Failed, should be empty


# if __name__ == "__main__":
#     # Run a simple test
#     asyncio.run(test_get_travel_info_empty_destinations())
#     print("Basic tests passed!")