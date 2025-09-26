import cloudinary
import cloudinary.uploader
import cloudinary.api
import io
import base64
from typing import List, Optional, Union
from fastapi import UploadFile, HTTPException
# from PIL import Image  # Comment out until needed

from app.core.config import settings

# Configure Cloudinary
cloudinary.config(
    cloud_name=settings.CLOUDINARY_CLOUD_NAME,
    api_key=settings.CLOUDINARY_API_KEY,
    api_secret=settings.CLOUDINARY_API_SECRET
)

class CloudinaryService:
    """Enhanced Cloudinary service for WiseBite image management"""
    
    @staticmethod
    def _validate_image(file: UploadFile) -> None:
        """Validate uploaded image file"""
        # Check file size (max 10MB)
        if hasattr(file, 'size') and file.size > 10 * 1024 * 1024:
            raise HTTPException(status_code=400, detail="File size too large. Max 10MB allowed.")
        
        # Check file type - support common image types
        allowed_types = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp', 'image/gif', 'image/*']
        if file.content_type and file.content_type not in allowed_types:
            raise HTTPException(
                status_code=400, 
                detail=f"Invalid file type '{file.content_type}'. Supported: JPEG, PNG, WebP, GIF"
            )

    @staticmethod
    def upload_avatar(file: UploadFile, user_id: str) -> str:
        """Upload user avatar image"""
        try:
            CloudinaryService._validate_image(file)
            
            result = cloudinary.uploader.upload(
                file.file,
                folder="wisebite/avatars",
                public_id=f"avatar_{user_id}",
                overwrite=True,
                resource_type="image",
                transformation=[
                    {"width": 200, "height": 200, "crop": "fill", "gravity": "face"},
                    {"quality": "auto:best"}
                ]
            )
            return result.get("secure_url", "")
        except HTTPException:
            raise
        except Exception as e:
            print(f"Error uploading avatar to Cloudinary: {e}")
            raise HTTPException(status_code=500, detail="Failed to upload avatar")

    @staticmethod
    def upload_store_image(file: UploadFile, store_id: str) -> str:
        """Upload store logo/image"""
        try:
            CloudinaryService._validate_image(file)
            
            result = cloudinary.uploader.upload(
                file.file,
                folder="wisebite/stores",
                public_id=f"store_{store_id}",
                overwrite=True,
                resource_type="image",
                transformation=[
                    {"width": 400, "height": 300, "crop": "fill"},
                    {"quality": "auto:good"}
                ]
            )
            return result.get("secure_url", "")
        except HTTPException:
            raise
        except Exception as e:
            print(f"Error uploading store image to Cloudinary: {e}")
            raise HTTPException(status_code=500, detail="Failed to upload store image")

    @staticmethod
    def upload_food_item_image(file: UploadFile, food_item_id: str) -> str:
        """Upload food item image"""
        try:
            CloudinaryService._validate_image(file)
            
            result = cloudinary.uploader.upload(
                file.file,
                folder="wisebite/food_items",
                public_id=f"food_{food_item_id}",
                overwrite=True,
                resource_type="image",
                transformation=[
                    {"width": 500, "height": 400, "crop": "fill"},
                    {"quality": "auto:good"}
                ]
            )
            return result.get("secure_url", "")
        except HTTPException:
            raise
        except Exception as e:
            print(f"Error uploading food item image to Cloudinary: {e}")
            raise HTTPException(status_code=500, detail="Failed to upload food item image")

    @staticmethod
    def upload_surprise_bag_images(files: List[UploadFile], bag_id: str) -> List[str]:
        """Upload multiple images for surprise bag"""
        try:
            urls = []
            for i, file in enumerate(files[:3]):  # Max 3 images
                CloudinaryService._validate_image(file)
                
                result = cloudinary.uploader.upload(
                    file.file,
                    folder="wisebite/surprise_bags",
                    public_id=f"bag_{bag_id}_{i+1}",
                    overwrite=True,
                    resource_type="image",
                    transformation=[
                        {"width": 600, "height": 400, "crop": "fill"},
                        {"quality": "auto:good"}
                    ]
                )
                urls.append(result.get("secure_url", ""))
            
            return urls
        except HTTPException:
            raise
        except Exception as e:
            print(f"Error uploading surprise bag images to Cloudinary: {e}")
            raise HTTPException(status_code=500, detail="Failed to upload surprise bag images")

    @staticmethod
    def upload_base64_image(base64_data: str, folder: str, public_id: str) -> str:
        """Upload base64 encoded image (for mobile app)"""
        try:
            # Remove data:image/jpeg;base64, prefix if present
            if base64_data.startswith('data:image'):
                base64_data = base64_data.split(',')[1]
            
            result = cloudinary.uploader.upload(
                f"data:image/jpeg;base64,{base64_data}",
                folder=f"wisebite/{folder}",
                public_id=public_id,
                overwrite=True,
                resource_type="image",
                transformation=[
                    {"quality": "auto:good"}
                ]
            )
            return result.get("secure_url", "")
        except Exception as e:
            print(f"Error uploading base64 image to Cloudinary: {e}")
            raise HTTPException(status_code=500, detail="Failed to upload image")

    @staticmethod
    def delete_image(public_id: str) -> bool:
        """Delete image from Cloudinary"""
        try:
            result = cloudinary.uploader.destroy(public_id)
            return result.get('result') == 'ok'
        except Exception as e:
            print(f"Error deleting image from Cloudinary: {e}")
            return False

    @staticmethod
    def get_optimized_url(original_url: str, width: int = None, height: int = None, quality: str = "auto:good") -> str:
        """Get optimized version of existing Cloudinary image"""
        try:
            # Extract public_id from URL
            parts = original_url.split('/')
            public_id = '/'.join(parts[7:])  # Remove cloudinary base URL parts
            public_id = public_id.split('.')[0]  # Remove file extension
            
            transformations = {"quality": quality}
            if width:
                transformations["width"] = width
            if height:
                transformations["height"] = height
                transformations["crop"] = "fill"
            
            return cloudinary.CloudinaryImage(public_id).build_url(**transformations)
        except Exception as e:
            print(f"Error creating optimized URL: {e}")
            return original_url

# Legacy functions for backward compatibility
def upload_avatar(file: UploadFile, public_id: str) -> str:
    """Legacy function - use CloudinaryService.upload_avatar instead"""
    return CloudinaryService.upload_avatar(file, public_id)

def upload_category_icon(file: UploadFile, public_id: str) -> str:
    """Legacy function - use CloudinaryService.upload_store_image instead"""
    return CloudinaryService.upload_store_image(file, public_id)

def upload_order_image(file1: UploadFile, file2: UploadFile, public_id: str) -> List[str]:
    """Legacy function - use CloudinaryService.upload_surprise_bag_images instead"""
    return CloudinaryService.upload_surprise_bag_images([file1, file2], public_id)

def upload_static_image(file: UploadFile, public_id: str) -> str:
    """Legacy function - use CloudinaryService.upload_food_item_image instead"""
    return CloudinaryService.upload_food_item_image(file, public_id)