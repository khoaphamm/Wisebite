
from app.models import ConversationType
from pydantic import BaseModel
from datetime import datetime
import uuid

class MessageBase(BaseModel):
    conversation_id: uuid.UUID
    content: str

class MessagePublic(MessageBase):
    id: uuid.UUID
    sender_id: uuid.UUID
    created_at: datetime

class ConversationBase(BaseModel):
    name: str | None = None
    type: ConversationType = ConversationType.PRIVATE

class ConversationCreate(ConversationBase):
    member_ids: list[uuid.UUID]

class ConversationMember(BaseModel):
    user_id: uuid.UUID

class ConversationPublic(ConversationBase):
    id: uuid.UUID
    last_message_id: uuid.UUID | None
    members: list[ConversationMember] = []
    last_message: MessagePublic | None = None
    created_at: datetime
    updated_at: datetime

class MessageCreate(MessageBase):
    pass


