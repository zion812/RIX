-- Database migrations for ROSTRY Platform MVP

-- Users table with roles and KYC states
CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('general', 'farmer', 'breeder', 'admin')),
    kyc_state VARCHAR(20) NOT NULL DEFAULT 'unverified' CHECK (kyc_state IN ('unverified', 'pending', 'verified')),
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    location JSONB, -- {lat, lng, address}
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Fowls table with lineage tracking
CREATE TABLE fowls (
    id UUID PRIMARY KEY,
    owner_id UUID NOT NULL REFERENCES users(id),
    breed VARCHAR(100) NOT NULL,
    gender VARCHAR(10) CHECK (gender IN ('male', 'female')),
    date_of_birth DATE NOT NULL,
    parent_ids UUID[], -- Array of parent fowl IDs
    breeder_ready BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Fowl records for lifecycle events
CREATE TABLE fowl_records (
    id UUID PRIMARY KEY,
    fowl_id UUID NOT NULL REFERENCES fowls(id) ON DELETE CASCADE,
    record_type VARCHAR(50) NOT NULL CHECK (record_type IN ('vaccination', '5-week', '20-week', 'weekly', 'breeder-ready')),
    details JSONB, -- Record-specific details
    proof_urls TEXT[], -- Array of proof image URLs
    recorded_at TIMESTAMPTZ DEFAULT NOW(),
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Marketplace listings
CREATE TABLE market_listings (
    id UUID PRIMARY KEY,
    fowl_id UUID NOT NULL REFERENCES fowls(id),
    seller_id UUID NOT NULL REFERENCES users(id),
    purpose VARCHAR(20) NOT NULL CHECK (purpose IN ('breeding', 'fighting', 'ornamental')),
    price_cents INTEGER NOT NULL,
    location JSONB, -- {lat, lng, address}
    status VARCHAR(20) NOT NULL DEFAULT 'active' CHECK (status IN ('active', 'sold', 'closed')),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- Transfer logs for verified transfers
CREATE TABLE transfer_logs (
    id UUID PRIMARY KEY,
    fowl_id UUID NOT NULL REFERENCES fowls(id),
    giver_id UUID NOT NULL REFERENCES users(id),
    receiver_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'verified', 'rejected')),
    verification_details JSONB, -- {photo_keys[], color, weight_kg, age_weeks, location, agreed_price_cents}
    initiated_at TIMESTAMPTZ DEFAULT NOW(),
    verified_at TIMESTAMPTZ,
    rejected_at TIMESTAMPTZ
);

-- Messages for 1:1 communication
CREATE TABLE messages (
    id UUID PRIMARY KEY,
    sender_id UUID NOT NULL REFERENCES users(id),
    receiver_id UUID NOT NULL REFERENCES users(id),
    thread_id UUID NOT NULL, -- Group messages by thread
    content TEXT NOT NULL,
    sent_at TIMESTAMPTZ DEFAULT NOW()
);

-- Coin ledger for transactions
CREATE TABLE coin_ledger (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    amount INTEGER NOT NULL, -- Positive for credit, negative for debit
    transaction_type VARCHAR(50) NOT NULL CHECK (transaction_type IN ('listing', 'transfer_verification', 'maintenance', 'admin_credit')),
    related_entity_id UUID, -- ID of related entity (listing, transfer, etc.)
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Indexes for performance
CREATE INDEX idx_fowls_owner_id ON fowls(owner_id);
CREATE INDEX idx_fowls_date_of_birth ON fowls(date_of_birth);
CREATE INDEX idx_fowl_records_fowl_id ON fowl_records(fowl_id);
CREATE INDEX idx_fowl_records_record_type ON fowl_records(record_type);
CREATE INDEX idx_market_listings_status ON market_listings(status);
CREATE INDEX idx_market_listings_price ON market_listings(price_cents);
CREATE INDEX idx_transfer_logs_giver_id ON transfer_logs(giver_id);
CREATE INDEX idx_transfer_logs_receiver_id ON transfer_logs(receiver_id);
CREATE INDEX idx_transfer_logs_status ON transfer_logs(status);
CREATE INDEX idx_messages_thread_id ON messages(thread_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_coin_ledger_user_id ON coin_ledger(user_id);
CREATE INDEX idx_coin_ledger_transaction_type ON coin_ledger(transaction_type);

-- Trigger to prevent direct owner changes (ownership should only change via verified transfers)
CREATE OR REPLACE FUNCTION prevent_direct_owner_change()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.owner_id != NEW.owner_id AND NOT EXISTS (
        SELECT 1 FROM transfer_logs 
        WHERE fowl_id = NEW.id 
        AND status = 'verified' 
        AND verified_at = NEW.updated_at
    ) THEN
        RAISE EXCEPTION 'Owner can only be changed via verified transfer';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER owner_change_trigger
BEFORE UPDATE OF owner_id ON fowls
FOR EACH ROW EXECUTE FUNCTION prevent_direct_owner_change();

-- Function to update fowl's updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_fowls_updated_at 
BEFORE UPDATE ON fowls 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_listings_updated_at 
BEFORE UPDATE ON market_listings 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at 
BEFORE UPDATE ON users 
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();