"""Add hierarchical categories and enhanced inventory management

Revision ID: add_categories_inventory
Revises: previous_revision
Create Date: 2025-09-30 12:00:00.000000

"""
from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects import postgresql
import uuid

# revision identifiers, used by Alembic.
revision = 'add_categories_inventory'
down_revision = None  # Update this to your latest revision
branch_labels = None
depends_on = None

def upgrade() -> None:
    # Create Category table
    op.create_table('category',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False, default=uuid.uuid4),
        sa.Column('name', sa.String(length=255), nullable=False),
        sa.Column('parent_category_id', postgresql.UUID(as_uuid=True), nullable=True),
        sa.Column('description', sa.String(length=500), nullable=True),
        sa.Column('is_active', sa.Boolean(), nullable=False, default=True),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.ForeignKeyConstraint(['parent_category_id'], ['category.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_category_name'), 'category', ['name'], unique=False)

    # Add new columns to existing fooditem table
    op.add_column('fooditem', sa.Column('sku', sa.String(length=100), nullable=True))
    op.add_column('fooditem', sa.Column('standard_price', sa.Float(), nullable=True))
    op.add_column('fooditem', sa.Column('cost_price', sa.Float(), nullable=True))
    op.add_column('fooditem', sa.Column('is_fresh', sa.Boolean(), nullable=False, default=True))
    op.add_column('fooditem', sa.Column('surplus_quantity', sa.Integer(), nullable=False, default=0))
    op.add_column('fooditem', sa.Column('reserved_quantity', sa.Integer(), nullable=False, default=0))
    op.add_column('fooditem', sa.Column('available_quantity', sa.Integer(), nullable=False, default=0))
    op.add_column('fooditem', sa.Column('is_marked_for_surplus', sa.Boolean(), nullable=False, default=False))
    op.add_column('fooditem', sa.Column('surplus_discount_percentage', sa.Float(), nullable=True))
    op.add_column('fooditem', sa.Column('surplus_price', sa.Float(), nullable=True))
    op.add_column('fooditem', sa.Column('marked_surplus_at', sa.DateTime(), nullable=True))
    op.add_column('fooditem', sa.Column('weight', sa.Float(), nullable=True))
    op.add_column('fooditem', sa.Column('unit', sa.String(length=20), nullable=True, default='piece'))
    op.add_column('fooditem', sa.Column('is_active', sa.Boolean(), nullable=False, default=True))
    op.add_column('fooditem', sa.Column('updated_at', sa.DateTime(), nullable=False))
    op.add_column('fooditem', sa.Column('last_inventory_update', sa.DateTime(), nullable=True))
    op.add_column('fooditem', sa.Column('category_id', postgresql.UUID(as_uuid=True), nullable=True))
    op.add_column('fooditem', sa.Column('total_quantity', sa.Integer(), nullable=False, default=0))
    
    # Rename existing columns for consistency
    op.alter_column('fooditem', 'original_price', new_column_name='standard_price')
    op.alter_column('fooditem', 'quantity', new_column_name='total_quantity')
    op.alter_column('fooditem', 'name', type_=sa.String(length=255))
    
    # Add foreign key for category
    op.create_foreign_key(None, 'fooditem', 'category', ['category_id'], ['id'])
    op.create_index(op.f('ix_fooditem_sku'), 'fooditem', ['sku'], unique=False)
    op.create_index(op.f('ix_fooditem_store_id'), 'fooditem', ['store_id'], unique=False)

    # Create InventoryLog table
    op.create_table('inventorylog',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False, default=uuid.uuid4),
        sa.Column('food_item_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('change_type', sa.String(length=50), nullable=False),
        sa.Column('quantity_change', sa.Integer(), nullable=False),
        sa.Column('previous_quantity', sa.Integer(), nullable=False),
        sa.Column('new_quantity', sa.Integer(), nullable=False),
        sa.Column('reason', sa.String(length=255), nullable=True),
        sa.Column('reference_id', postgresql.UUID(as_uuid=True), nullable=True),
        sa.Column('created_at', sa.DateTime(), nullable=False),
        sa.ForeignKeyConstraint(['food_item_id'], ['fooditem.id'], ),
        sa.PrimaryKeyConstraint('id')
    )
    op.create_index(op.f('ix_inventorylog_food_item_id'), 'inventorylog', ['food_item_id'], unique=False)

    # Update SurpriseBag table
    op.add_column('surprisebag', sa.Column('bag_type', sa.String(length=50), nullable=False, default='combo'))
    op.add_column('surprisebag', sa.Column('discount_percentage', sa.Float(), nullable=False, default=0.5))
    op.add_column('surprisebag', sa.Column('max_per_customer', sa.Integer(), nullable=False, default=1))
    op.add_column('surprisebag', sa.Column('available_from', sa.DateTime(), nullable=False))
    op.add_column('surprisebag', sa.Column('available_until', sa.DateTime(), nullable=False))
    op.add_column('surprisebag', sa.Column('is_active', sa.Boolean(), nullable=False, default=True))
    op.add_column('surprisebag', sa.Column('is_auto_generated', sa.Boolean(), nullable=False, default=False))
    op.add_column('surprisebag', sa.Column('created_at', sa.DateTime(), nullable=False))
    op.add_column('surprisebag', sa.Column('updated_at', sa.DateTime(), nullable=False))
    op.alter_column('surprisebag', 'name', type_=sa.String(length=255))
    op.create_index(op.f('ix_surprisebag_store_id'), 'surprisebag', ['store_id'], unique=False)

    # Create SurpriseBagItem table
    op.create_table('surprisebagitem',
        sa.Column('id', postgresql.UUID(as_uuid=True), nullable=False, default=uuid.uuid4),
        sa.Column('surprise_bag_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('food_item_id', postgresql.UUID(as_uuid=True), nullable=False),
        sa.Column('min_quantity', sa.Integer(), nullable=False, default=1),
        sa.Column('max_quantity', sa.Integer(), nullable=False, default=1),
        sa.Column('estimated_value_per_unit', sa.Float(), nullable=False),
        sa.Column('weight_in_selection', sa.Float(), nullable=False, default=1.0),
        sa.ForeignKeyConstraint(['food_item_id'], ['fooditem.id'], ),
        sa.ForeignKeyConstraint(['surprise_bag_id'], ['surprisebag.id'], ),
        sa.PrimaryKeyConstraint('id')
    )

def downgrade() -> None:
    # Drop new tables
    op.drop_table('surprisebagitem')
    op.drop_table('inventorylog')
    op.drop_table('category')
    
    # Remove new columns from existing tables
    op.drop_column('fooditem', 'category_id')
    op.drop_column('fooditem', 'last_inventory_update')
    op.drop_column('fooditem', 'updated_at')
    op.drop_column('fooditem', 'is_active')
    op.drop_column('fooditem', 'unit')
    op.drop_column('fooditem', 'weight')
    op.drop_column('fooditem', 'marked_surplus_at')
    op.drop_column('fooditem', 'surplus_price')
    op.drop_column('fooditem', 'surplus_discount_percentage')
    op.drop_column('fooditem', 'is_marked_for_surplus')
    op.drop_column('fooditem', 'available_quantity')
    op.drop_column('fooditem', 'reserved_quantity')
    op.drop_column('fooditem', 'surplus_quantity')
    op.drop_column('fooditem', 'is_fresh')
    op.drop_column('fooditem', 'cost_price')
    op.drop_column('fooditem', 'sku')
    
    # Revert column renames
    op.alter_column('fooditem', 'standard_price', new_column_name='original_price')
    op.alter_column('fooditem', 'total_quantity', new_column_name='quantity')
    
    # Remove new surprise bag columns
    op.drop_column('surprisebag', 'updated_at')
    op.drop_column('surprisebag', 'created_at')
    op.drop_column('surprisebag', 'is_auto_generated')
    op.drop_column('surprisebag', 'is_active')
    op.drop_column('surprisebag', 'available_until')
    op.drop_column('surprisebag', 'available_from')
    op.drop_column('surprisebag', 'max_per_customer')
    op.drop_column('surprisebag', 'discount_percentage')
    op.drop_column('surprisebag', 'bag_type')