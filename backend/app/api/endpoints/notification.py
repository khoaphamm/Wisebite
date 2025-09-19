
from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session
from app.models import User
from app.api.deps import SessionDep, CurrentUser, CurrentAdmin, get_db
from typing import List
import uuid
from app.schemas.notification import NotificationCreate, NotificationPublic
from app import crud

router = APIRouter(prefix="/notifications", tags=["notifications"])

@router.post("/send", response_model=NotificationPublic)
def send_notification(
    notification_in: NotificationCreate,
    user_ids: List[uuid.UUID],
    session: SessionDep,
    current_admin: CurrentAdmin
):
    notification = crud.create_notification(session, notification_in, user_ids)
    return notification

@router.post("/send_all", response_model=NotificationPublic)
def send_notification_to_all(
    notification_in: NotificationCreate,
    session: SessionDep,
    current_admin: CurrentAdmin
):
    notification = crud.create_notification_to_all(session, notification_in)
    return notification


@router.get("/", response_model=List[NotificationPublic])
def get_notifications(
    session: SessionDep,
    current_admin: CurrentAdmin
):
    notifications = crud.get_all_notifications(session)
    return notifications
