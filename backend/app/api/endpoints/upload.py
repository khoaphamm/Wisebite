from fastapi import APIRouter, Depends, File, UploadFile, HTTPException, Form
from typing import List, Optional
from sqlmodel import Session
from app.api.deps import CurrentUser, SessionDep
from app.models import User
from app.schemas.user import UserUpdate
from app.services.upload import CloudinaryService
from app import crud

router = APIRouter()

@router.post("/avatar")
async def upload_user_avatar(
    current_user: CurrentUser,
    session: SessionDep,
    file: UploadFile = File(...)
):
    """Upload user avatar image"""
    try:
        # Upload to Cloudinary
        image_url = CloudinaryService.upload_avatar(file, str(current_user.id))
        
        # Update user profile with new avatar URL
        user_update = UserUpdate(avt_url=image_url)
        updated_user = crud.update_user(session, current_user, user_update)
        
        return {
            "success": True,
            "message": "Avatar uploaded successfully",
            "image_url": image_url,
            "user": updated_user
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to upload avatar: {str(e)}")

@router.post("/store/{store_id}/image")
async def upload_store_image(
    store_id: int,
    current_user: CurrentUser,
    session: SessionDep,
    file: UploadFile = File(...)
):
    """Upload store image (only store owner can upload)"""
    try:
        # Verify user owns the store
        store = crud.get_store(session, store_id)
        if not store:
            raise HTTPException(status_code=404, detail="Store not found")
        
        if store.owner_id != current_user.id:
            raise HTTPException(status_code=403, detail="Not authorized to upload image for this store")
        
        # Upload to Cloudinary
        image_url = CloudinaryService.upload_store_image(file, str(store_id))
        
        # Update store with new image URL
        store_update = {"image_url": image_url}
        updated_store = crud.update_store(session, store_id, store_update)
        
        return {
            "success": True,
            "message": "Store image uploaded successfully",
            "image_url": image_url,
            "store": updated_store
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to upload store image: {str(e)}")

@router.post("/food-item/{food_item_id}/image")
async def upload_food_item_image(
    food_item_id: int,
    current_user: CurrentUser,
    session: SessionDep,
    file: UploadFile = File(...)
):
    """Upload food item image"""
    try:
        # Verify user owns the store that has this food item
        food_item = crud.get_food_item(session, food_item_id)
        if not food_item:
            raise HTTPException(status_code=404, detail="Food item not found")
        
        store = crud.get_store(session, food_item.store_id)
        if store.owner_id != current_user.id:
            raise HTTPException(status_code=403, detail="Not authorized to upload image for this food item")
        
        # Upload to Cloudinary
        image_url = CloudinaryService.upload_food_item_image(file, str(food_item_id))
        
        # Update food item with new image URL
        food_item_update = {"image_url": image_url}
        updated_food_item = crud.update_food_item(session, food_item_id, food_item_update)
        
        return {
            "success": True,
            "message": "Food item image uploaded successfully",
            "image_url": image_url,
            "food_item": updated_food_item
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to upload food item image: {str(e)}")

@router.post("/surprise-bag/{bag_id}/images")
async def upload_surprise_bag_images(
    bag_id: int,
    current_user: CurrentUser,
    session: SessionDep,
    files: List[UploadFile] = File(...)
):
    """Upload multiple images for surprise bag"""
    try:
        if len(files) > 3:
            raise HTTPException(status_code=400, detail="Maximum 3 images allowed")
        
        # Verify user owns the store that has this surprise bag
        surprise_bag = crud.get_surprise_bag(session, bag_id)
        if not surprise_bag:
            raise HTTPException(status_code=404, detail="Surprise bag not found")
        
        store = crud.get_store(session, surprise_bag.store_id)
        if store.owner_id != current_user.id:
            raise HTTPException(status_code=403, detail="Not authorized to upload images for this surprise bag")
        
        # Upload to Cloudinary
        image_urls = CloudinaryService.upload_surprise_bag_images(files, str(bag_id))
        
        # Update surprise bag with new image URLs
        bag_update = {"image_urls": image_urls}
        updated_bag = crud.update_surprise_bag(session, bag_id, bag_update)
        
        return {
            "success": True,
            "message": f"{len(image_urls)} images uploaded successfully",
            "image_urls": image_urls,
            "surprise_bag": updated_bag
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to upload surprise bag images: {str(e)}")

@router.post("/base64")
async def upload_base64_image(
    current_user: CurrentUser,
    folder: str = Form(...),
    public_id: str = Form(...),
    base64_data: str = Form(...)
):
    """Upload base64 encoded image (useful for mobile apps)"""
    try:
        # Validate folder
        allowed_folders = ["avatars", "stores", "food_items", "surprise_bags", "general"]
        if folder not in allowed_folders:
            raise HTTPException(status_code=400, detail=f"Invalid folder. Allowed: {allowed_folders}")
        
        # Upload to Cloudinary
        image_url = CloudinaryService.upload_base64_image(base64_data, folder, public_id)
        
        return {
            "success": True,
            "message": "Image uploaded successfully",
            "image_url": image_url
        }
    except HTTPException:
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to upload image: {str(e)}")

@router.delete("/image")
async def delete_image(
    public_id: str,
    current_user: CurrentUser
):
    """Delete image from Cloudinary"""
    try:
        success = CloudinaryService.delete_image(public_id)
        
        if success:
            return {
                "success": True,
                "message": "Image deleted successfully"
            }
        else:
            return {
                "success": False,
                "message": "Failed to delete image"
            }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to delete image: {str(e)}")

@router.get("/optimize")
async def get_optimized_url(
    original_url: str,
    width: Optional[int] = None,
    height: Optional[int] = None,
    quality: str = "auto:good"
):
    """Get optimized version of existing Cloudinary image"""
    try:
        optimized_url = CloudinaryService.get_optimized_url(original_url, width, height, quality)
        
        return {
            "success": True,
            "original_url": original_url,
            "optimized_url": optimized_url
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Failed to optimize image: {str(e)}")