-- ============================================
-- LIBRARY APP - DATABASE SCHEMA
-- File: schema.sql
-- Run this FIRST to create the database structure
-- ============================================

-- Clean up existing tables (Order matters due to foreign keys)
DROP TABLE IF EXISTS personal_challenge_books CASCADE;
DROP TABLE IF EXISTS personal_challenges CASCADE;
DROP TABLE IF EXISTS user_app_challenges CASCADE;
DROP TABLE IF EXISTS app_challenges CASCADE;
DROP TABLE IF EXISTS coming_soon_books CASCADE;
DROP TABLE IF EXISTS reviews CASCADE;
DROP TABLE IF EXISTS user_books CASCADE;
DROP TABLE IF EXISTS followers CASCADE;
DROP TABLE IF EXISTS books CASCADE;
DROP TABLE IF EXISTS authors CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- ============================================
-- 1. USERS TABLE
-- ============================================
CREATE TABLE users (
    user_id SERIAL PRIMARY KEY, -- Auto-incrementing ID
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL, -- Storing BCrypt hash, never plain text
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER' CHECK (role IN ('USER', 'ADMIN')),
    bio TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. AUTHORS TABLE
-- ============================================
CREATE TABLE authors (
    author_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    biography TEXT,
    birth_date DATE,
    death_date DATE,
    nationality VARCHAR(50),
    website VARCHAR(255),
    profile_image_url VARCHAR(255)
);

-- ============================================
-- 3. BOOKS TABLE
-- ============================================
CREATE TABLE books (
    book_id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author_id INTEGER NOT NULL REFERENCES authors(author_id) ON DELETE CASCADE, -- If author is deleted, delete their books
    genre VARCHAR(100),
    publication_year INTEGER,
    page_count INTEGER,
    description TEXT,
    cover_image_url VARCHAR(255),
    average_rating DECIMAL(3,2) DEFAULT 0.00, -- Default 0 to avoid NULL math
    rating_count INTEGER DEFAULT 0
);

-- ============================================
-- 4. USER_BOOKS TABLE (Junction Table)
-- Connects Users <-> Books (Many-to-Many)
-- ============================================
CREATE TABLE user_books (
    user_book_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL CHECK (status IN ('TO_READ', 'READING', 'COMPLETED')),
    current_page INTEGER DEFAULT 0,
    start_date DATE,
    finish_date DATE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id) -- Prevents user from adding the same book twice
);

-- ============================================
-- 5. REVIEWS TABLE
-- ============================================
CREATE TABLE reviews (
    review_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, book_id) -- One review per book per user
);

-- ============================================
-- 6. FOLLOWERS TABLE 
-- Self-referencing Many-to-Many relationship
-- ============================================
CREATE TABLE followers (
    follower_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    following_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    followed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (follower_id, following_id),
    CHECK (follower_id != following_id) -- Prevent users from following themselves
);

-- ============================================
-- 7. COMING_SOON_BOOKS TABLE
-- ============================================
CREATE TABLE coming_soon_books (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(100) NOT NULL,
    genre VARCHAR(100),
    description TEXT,
    release_date DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'UPCOMING' CHECK (status IN ('UPCOMING', 'RELEASED', 'DELAYED')),
    cover_image VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 8. APP_CHALLENGES TABLE
-- Global challenges created by admins
-- ============================================
CREATE TABLE app_challenges (
    challenge_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    challenge_type VARCHAR(50) NOT NULL CHECK (challenge_type IN ('SEASONAL', 'GENRE', 'READING_GOAL', 'EVENT')),
    target INTEGER NOT NULL,
    required_genre VARCHAR(50),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    badge_name VARCHAR(100),
    badge_icon VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 9. USER_APP_CHALLENGES TABLE
-- Tracks progress on global challenges
-- ============================================
CREATE TABLE user_app_challenges (
    id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    challenge_id INTEGER NOT NULL REFERENCES app_challenges(challenge_id) ON DELETE CASCADE,
    current_progress INTEGER DEFAULT 0,
    completed BOOLEAN DEFAULT FALSE,
    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    UNIQUE(user_id, challenge_id)
);

-- ============================================
-- 10. PERSONAL_CHALLENGES TABLE
-- Custom goals created by users
-- ============================================
CREATE TABLE personal_challenges (
    challenge_id SERIAL PRIMARY KEY,
    user_id INTEGER NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    target_books INTEGER NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 11. PERSONAL_CHALLENGE_BOOKS TABLE
-- Junction table for personal challenges
-- ============================================
CREATE TABLE personal_challenge_books (
    challenge_book_id SERIAL PRIMARY KEY,
    challenge_id INTEGER NOT NULL REFERENCES personal_challenges(challenge_id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL REFERENCES books(book_id) ON DELETE CASCADE,
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(challenge_id, book_id)
);

-- ============================================
-- INDEXES
-- Added for performance on commonly queried columns
-- ============================================
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author_id);
CREATE INDEX idx_books_genre ON books(genre);
CREATE INDEX idx_user_books_user ON user_books(user_id);
CREATE INDEX idx_user_books_status ON user_books(status);
CREATE INDEX idx_reviews_book ON reviews(book_id);
CREATE INDEX idx_reviews_user ON reviews(user_id);
CREATE INDEX idx_followers_follower ON followers(follower_id);
CREATE INDEX idx_followers_following ON followers(following_id);
CREATE INDEX idx_coming_soon_status ON coming_soon_books(status);
CREATE INDEX idx_coming_soon_release ON coming_soon_books(release_date);
CREATE INDEX idx_app_challenges_active ON app_challenges(is_active);
CREATE INDEX idx_user_app_challenges_user ON user_app_challenges(user_id);
CREATE INDEX idx_user_app_challenges_completed ON user_app_challenges(completed);
CREATE INDEX idx_personal_challenges_user ON personal_challenges(user_id);

-- ============================================
-- TRIGGER: Auto-update book average rating
-- This saves us from calculating averages on every read
-- ============================================
CREATE OR REPLACE FUNCTION update_book_rating()
RETURNS TRIGGER AS $$
BEGIN
    UPDATE books
    SET average_rating = (
        SELECT COALESCE(ROUND(AVG(rating)::numeric, 2), 0)
        FROM reviews
        WHERE book_id = COALESCE(NEW.book_id, OLD.book_id)
    ),
    rating_count = (
        SELECT COUNT(*)
        FROM reviews
        WHERE book_id = COALESCE(NEW.book_id, OLD.book_id)
    )
    WHERE book_id = COALESCE(NEW.book_id, OLD.book_id);
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_update_book_rating
AFTER INSERT OR UPDATE OR DELETE ON reviews
FOR EACH ROW
EXECUTE FUNCTION update_book_rating();

-- ============================================
-- TRIGGER: Auto-mark coming soon books as RELEASED
-- ============================================
CREATE OR REPLACE FUNCTION check_release_date()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.release_date <= CURRENT_DATE THEN
        NEW.status := 'RELEASED';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_check_release_date
BEFORE INSERT OR UPDATE ON coming_soon_books
FOR EACH ROW
EXECUTE FUNCTION check_release_date();