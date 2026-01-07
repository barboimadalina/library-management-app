-- ============================================
-- LIBRARY APP - SAMPLE DATA
-- File: sample_data.sql
-- Run this AFTER schema.sql
-- ============================================

-- ============================================
-- 1. USERS (25 users)
-- NOTE: All passwords are set to "password123" for testing
-- The hashes are pre-generated using BCrypt
-- ============================================
INSERT INTO users (username, email, password_hash, full_name, role, bio) VALUES
('admin', 'admin@library.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Admin User', 'ADMIN', 'System administrator'),
('sarah_books', 'sarah@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Sarah Johnson', 'USER', 'Fantasy lover'),
('john_reader', 'john@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'John Smith', 'USER', 'Reading enthusiast'),
('emma_writes', 'emma@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Emma Wilson', 'USER', 'Mystery fan'),
('michael_lit', 'michael@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Michael Brown', 'USER', 'Classic literature'),
('lisa_romance', 'lisa@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Lisa Anderson', 'USER', 'Romance novels'),
('david_scifi', 'david@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'David Martinez', 'USER', 'Sci-fi fan'),
('sophia_ya', 'sophia@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Sophia Garcia', 'USER', 'YA fiction'),
('james_thriller', 'james@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'James Rodriguez', 'USER', 'Thriller lover'),
('olivia_books', 'olivia@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Olivia Taylor', 'USER', 'Book collector'),
('william_reads', 'william@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'William Lee', 'USER', 'Adventure books'),
('ava_poetry', 'ava@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Ava White', 'USER', 'Poetry enthusiast'),
('robert_history', 'robert@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Robert Harris', 'USER', 'History buff'),
('isabella_art', 'isabella@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Isabella Martin', 'USER', 'Art books'),
('daniel_horror', 'daniel@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Daniel Thompson', 'USER', 'Horror stories'),
('mia_memoir', 'mia@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Mia Moore', 'USER', 'Biography reader'),
('alex_travel', 'alex@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Alexander Jackson', 'USER', 'Travel enthusiast'),
('charlotte_drama', 'charlotte@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Charlotte Clark', 'USER', 'Drama novels'),
('matthew_tech', 'matthew@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Matthew Lewis', 'USER', 'Tech books'),
('amelia_cook', 'amelia@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Amelia Walker', 'USER', 'Food writing'),
('ryan_comics', 'ryan@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Ryan Hall', 'USER', 'Graphic novels'),
('ella_future', 'ella@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Ella Allen', 'USER', 'Dystopian fiction'),
('nathan_code', 'nathan@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Nathan Young', 'USER', 'Programming'),
('grace_magic', 'grace@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Grace King', 'USER', 'Magical worlds'),
('ethan_sports', 'ethan@email.com', '$2a$10$DHam3QGW.u2RMeqH5OJtFuauybY.lEd.BRQ5tYxb.Pxr/I.WfZqii', 'Ethan Wright', 'USER', 'Sports books');

-- ============================================
-- 2. AUTHORS (25 authors)
-- ============================================
INSERT INTO authors (name, biography, nationality) VALUES
('J.K. Rowling', 'British author, best known as the creator of the Harry Potter fantasy series', 'British'),
('George R.R. Martin', 'American novelist and short story writer, author of A Song of Ice and Fire', 'American'),
('Stephen King', 'American author of horror, supernatural fiction, and suspense novels', 'American'),
('Agatha Christie', 'English writer known for her detective novels featuring Hercule Poirot', 'British'),
('Jane Austen', 'English novelist known for her six major novels about the British landed gentry', 'British'),
('Ernest Hemingway', 'American novelist and journalist, known for his economical writing style', 'American'),
('F. Scott Fitzgerald', 'American novelist and short story writer of the Jazz Age', 'American'),
('Gabriel García Márquez', 'Colombian novelist, known for his magical realism works', 'Colombian'),
('Haruki Murakami', 'Japanese writer known for his surrealist and melancholic narratives', 'Japanese'),
('Margaret Atwood', 'Canadian poet, novelist, and environmental activist', 'Canadian'),
('Neil Gaiman', 'English author of short fiction, novels, comic books, and films', 'British'),
('Brandon Sanderson', 'American author of epic fantasy and science fiction', 'American'),
('Gillian Flynn', 'American author and former television critic, known for psychological thrillers', 'American'),
('Dan Brown', 'American author best known for his thriller novels', 'American'),
('Colleen Hoover', 'American author who primarily writes romance and young adult fiction', 'American'),
('John Green', 'American author and YouTube content creator, known for young adult fiction', 'American'),
('Suzanne Collins', 'American author best known for The Hunger Games trilogy', 'American'),
('Toni Morrison', 'American novelist and Nobel Prize in Literature winner', 'American'),
('Paulo Coelho', 'Brazilian lyricist and novelist, best known for The Alchemist', 'Brazilian'),
('Khaled Hosseini', 'Afghan-American novelist and physician, author of The Kite Runner', 'Afghan-American'),
('Sally Rooney', 'Irish author and screenwriter known for contemporary literary fiction', 'Irish'),
('Taylor Jenkins Reid', 'American author known for historical fiction novels', 'American'),
('Celeste Ng', 'American writer and novelist known for literary fiction', 'American'),
('Fredrik Backman', 'Swedish author and blogger known for heartwarming novels', 'Swedish'),
('Leigh Bardugo', 'American author best known for the Grishaverse fantasy series', 'American');

-- ============================================
-- 3. BOOKS (30 books)
-- ============================================
INSERT INTO books (title, author_id, genre, publication_year, page_count, description, cover_image_url, average_rating, rating_count) VALUES
('Harry Potter and the Philosophers Stone', 1, 'Fantasy', 1997, 223, 'A young wizard discovers his magical heritage and begins his journey at Hogwarts School of Witchcraft and Wizardry', NULL, 0, 0),
('Harry Potter and the Chamber of Secrets', 1, 'Fantasy', 1998, 251, 'Harrys second year at Hogwarts brings mysterious attacks and dark secrets', NULL, 0, 0),
('A Game of Thrones', 2, 'Fantasy', 1996, 694, 'The first book in the epic fantasy series A Song of Ice and Fire, set in the continent of Westeros', NULL, 0, 0),
('The Shining', 3, 'Horror', 1977, 447, 'A family heads to an isolated hotel for the winter where a sinister presence influences the father', NULL, 0, 0),
('It', 3, 'Horror', 1986, 1138, 'A group of children face an evil clown that emerges from the sewers every 27 years', NULL, 0, 0),
('Murder on the Orient Express', 4, 'Mystery', 1934, 256, 'Detective Hercule Poirot investigates a murder on a stranded train', NULL, 0, 0),
('And Then There Were None', 4, 'Mystery', 1939, 272, 'Ten strangers are lured to an island and are killed one by one', NULL, 0, 0),
('Pride and Prejudice', 5, 'Romance', 1813, 279, 'The story of Elizabeth Bennet and Mr. Darcy in Regency-era England', NULL, 0, 0),
('The Old Man and the Sea', 6, 'Literary Fiction', 1952, 127, 'An aging Cuban fisherman struggles with a giant marlin far out in the Gulf Stream', NULL, 0, 0),
('The Great Gatsby', 7, 'Literary Fiction', 1925, 180, 'A story of wealth, love, and the American Dream in the Jazz Age', NULL, 0, 0),
('One Hundred Years of Solitude', 8, 'Magical Realism', 1967, 417, 'The multi-generational story of the Buendia family in the fictional town of Macondo', NULL, 0, 0),
('Norwegian Wood', 9, 'Literary Fiction', 1987, 296, 'A nostalgic story of loss and sexuality set in 1960s Japan', NULL, 0, 0),
('The Handmaids Tale', 10, 'Dystopian', 1985, 311, 'A dystopian future where women are subjugated in a totalitarian theocracy', NULL, 0, 0),
('American Gods', 11, 'Fantasy', 2001, 465, 'A man discovers the gods of mythology are real and at war with new American gods', NULL, 0, 0),
('The Way of Kings', 12, 'Fantasy', 2010, 1007, 'The first book in the epic Stormlight Archive series', NULL, 0, 0),
('Gone Girl', 13, 'Thriller', 2012, 415, 'A psychological thriller about a marriage gone terribly wrong', NULL, 0, 0),
('The Da Vinci Code', 14, 'Thriller', 2003, 454, 'A symbologist investigates a murder in the Louvre and discovers a secret society', NULL, 0, 0),
('It Ends with Us', 15, 'Romance', 2016, 376, 'A young woman in a complicated relationship must make a difficult choice', NULL, 0, 0),
('The Fault in Our Stars', 16, 'Young Adult', 2012, 313, 'Two teenagers with cancer fall in love at a support group', NULL, 0, 0),
('The Hunger Games', 17, 'Young Adult', 2008, 374, 'In a dystopian future, teenagers fight to the death in an annual televised event', NULL, 0, 0),
('Beloved', 18, 'Historical Fiction', 1987, 321, 'A former slave is haunted by her past and the ghost of her daughter', NULL, 0, 0),
('The Alchemist', 19, 'Fiction', 1988, 197, 'A young shepherd travels from Spain to Egypt in search of treasure and discovers his destiny', NULL, 0, 0),
('The Kite Runner', 20, 'Historical Fiction', 2003, 371, 'A story of friendship, betrayal, and redemption set in Afghanistan', NULL, 0, 0),
('Normal People', 21, 'Contemporary', 2018, 266, 'The complicated relationship between two Irish teenagers into adulthood', NULL, 0, 0),
('The Seven Husbands of Evelyn Hugo', 22, 'Historical Fiction', 2017, 388, 'An aging Hollywood star reveals the truth about her glamorous life', NULL, 0, 0),
('Little Fires Everywhere', 23, 'Contemporary', 2017, 338, 'The intertwined fates of two families in a wealthy Ohio suburb', NULL, 0, 0),
('A Man Called Ove', 24, 'Contemporary', 2012, 337, 'A curmudgeonly man finds his life changed by his new neighbors', NULL, 0, 0),
('Six of Crows', 25, 'Fantasy', 2015, 462, 'A crew of criminals attempt an impossible heist in a fantasy world', NULL, 0, 0),
('Circe', 10, 'Fantasy', 2018, 393, 'The story of the witch Circe from Greek mythology', NULL, 0, 0),
('1984', 10, 'Dystopian', 1949, 328, 'A dystopian novel about totalitarian surveillance and control', NULL, 0, 0);

-- ============================================
-- 4. USER_BOOKS (48 entries)
-- Status can be: TO_READ, READING, COMPLETED
-- ============================================
INSERT INTO user_books (user_id, book_id, status, start_date, finish_date) VALUES
-- Sarah (user_id = 2)
(2, 1, 'COMPLETED', '2024-01-15', '2024-01-20'),
(2, 2, 'COMPLETED', '2024-01-21', '2024-01-28'),
(2, 3, 'READING', '2024-12-01', NULL),
(2, 15, 'TO_READ', NULL, NULL),
(2, 28, 'TO_READ', NULL, NULL),
-- John (user_id = 3)
(3, 4, 'COMPLETED', '2024-03-01', '2024-03-10'),
(3, 6, 'COMPLETED', '2024-03-15', '2024-03-20'),
(3, 8, 'COMPLETED', '2024-04-01', '2024-04-10'),
(3, 16, 'READING', '2024-11-15', NULL),
(3, 19, 'TO_READ', NULL, NULL),
-- Emma (user_id = 4)
(4, 6, 'COMPLETED', '2024-02-15', '2024-02-18'),
(4, 7, 'COMPLETED', '2024-02-20', '2024-02-23'),
(4, 16, 'COMPLETED', '2024-06-01', '2024-06-15'),
(4, 17, 'READING', '2024-12-01', NULL),
-- Michael (user_id = 5)
(5, 8, 'COMPLETED', '2024-01-05', '2024-01-15'),
(5, 10, 'COMPLETED', '2024-03-01', '2024-03-08'),
(5, 12, 'READING', '2024-11-20', NULL),
-- Lisa (user_id = 6)
(6, 8, 'COMPLETED', '2024-02-01', '2024-02-10'),
(6, 18, 'COMPLETED', '2024-03-15', '2024-03-22'),
(6, 24, 'READING', '2024-12-05', NULL),
-- David (user_id = 7)
(7, 3, 'COMPLETED', '2024-01-10', '2024-02-20'),
(7, 14, 'READING', '2024-12-01', NULL),
-- Sophia (user_id = 8)
(8, 1, 'COMPLETED', '2024-05-01', '2024-05-07'),
(8, 2, 'COMPLETED', '2024-05-08', '2024-05-15'),
(8, 15, 'READING', '2024-11-25', NULL),
-- James (user_id = 9)
(9, 4, 'COMPLETED', '2024-04-15', '2024-04-22'),
(9, 5, 'COMPLETED', '2024-05-01', '2024-05-20'),
(9, 16, 'READING', '2024-12-02', NULL),
-- Olivia (user_id = 10)
(10, 1, 'COMPLETED', '2024-07-01', '2024-07-05'),
(10, 19, 'COMPLETED', '2024-08-01', '2024-08-10'),
(10, 20, 'READING', '2024-12-01', NULL),
-- William (user_id = 11)
(11, 14, 'COMPLETED', '2024-05-10', '2024-05-30'),
(11, 15, 'READING', '2024-11-15', NULL),
-- Ava (user_id = 12)
(12, 8, 'COMPLETED', '2024-06-01', '2024-06-15'),
(12, 10, 'COMPLETED', '2024-07-01', '2024-07-05'),
-- Robert (user_id = 13)
(13, 13, 'COMPLETED', '2024-03-01', '2024-03-15'),
(13, 30, 'READING', '2024-12-10', NULL),
-- Isabella (user_id = 14)
(14, 4, 'COMPLETED', '2024-08-01', '2024-08-10'),
(14, 5, 'COMPLETED', '2024-08-15', '2024-09-05'),
-- Daniel (user_id = 15)
(15, 22, 'COMPLETED', '2024-05-01', '2024-05-20'),
(15, 23, 'READING', '2024-11-20', NULL),
-- Mia (user_id = 16)
(16, 3, 'COMPLETED', '2024-04-01', '2024-05-15'),
(16, 11, 'COMPLETED', '2024-06-01', '2024-06-10'),
-- Alex (user_id = 17)
(17, 25, 'COMPLETED', '2024-07-15', '2024-08-20'),
(17, 27, 'READING', '2024-12-08', NULL),
-- Charlotte (user_id = 18)
(18, 26, 'COMPLETED', '2024-09-01', '2024-09-20'),
-- Matthew (user_id = 19)
(19, 22, 'COMPLETED', '2024-10-01', '2024-10-15'),
-- Amelia (user_id = 20)
(20, 28, 'COMPLETED', '2024-06-10', '2024-07-01');

-- ============================================
-- 5. REVIEWS (40 reviews)
-- Inserting here triggers the "update_book_rating" function automatically
-- ============================================
INSERT INTO reviews (user_id, book_id, rating, review_text) VALUES
(2, 1, 5, 'Absolutely magical! Started my love for reading.'),
(2, 2, 5, 'Just as good as the first one. The mystery deepens!'),
(3, 4, 4, 'Terrifying and atmospheric. King at his best.'),
(3, 6, 4, 'Classic mystery. Poirot never disappoints.'),
(3, 8, 4, 'Beautiful romance with witty dialogue.'),
(4, 6, 5, 'Poirot at his best! The ending is brilliant.'),
(4, 7, 5, 'Masterful mystery. Could not put it down.'),
(4, 16, 4, 'Dark and twisted. Kept me guessing.'),
(5, 8, 5, 'Timeless classic. Elizabeth Bennet is iconic.'),
(5, 10, 5, 'Fitzgeralds best work. Beautiful prose.'),
(6, 8, 5, 'My favorite romance novel of all time!'),
(6, 18, 5, 'Emotionally powerful. Had me in tears.'),
(7, 3, 5, 'Epic fantasy at its finest. Winter is coming!'),
(8, 1, 5, 'Pure magic. Introduced me to reading.'),
(8, 2, 4, 'Darker but captivating. Love the mystery.'),
(9, 4, 4, 'Creepy and unsettling. Perfect horror.'),
(9, 5, 4, 'Long but worth every page. Pennywise is terrifying.'),
(10, 1, 5, 'Never gets old. Read it every year.'),
(10, 19, 5, 'Beautiful coming-of-age story.'),
(11, 14, 4, 'Gaimans imagination is incredible.'),
(12, 8, 5, 'Elizabeth Bennet is my favorite character ever.'),
(12, 10, 4, 'The symbolism is beautiful and profound.'),
(13, 13, 4, 'Chillingly relevant to our times.'),
(14, 4, 4, 'The Shining gave me nightmares for weeks.'),
(14, 5, 5, 'IT is Kings masterpiece. Absolutely terrifying.'),
(15, 22, 4, 'Heavy but important. Beautifully written.'),
(16, 3, 5, 'Cannot wait for the next book. Addictive!'),
(16, 11, 4, 'Adventure at its best. Mind-bending.'),
(17, 25, 4, 'Dark academia perfection. Love the characters.'),
(18, 26, 5, 'Changed my perspective on life.'),
(19, 22, 5, 'Unforgettable story. Heartbreaking and hopeful.'),
(20, 28, 5, 'The best heist plot ever written!'),
(2, 3, 5, 'Game of Thrones is completely addictive.'),
(3, 16, 4, 'Psychological thriller perfection.'),
(5, 12, 4, 'Murakamis style is unique and beautiful.'),
(6, 24, 5, 'Ove is the most lovable grumpy character.'),
(7, 14, 4, 'Epic fantasy world-building at its finest.'),
(8, 15, 5, 'Sanderson is a genius. Best magic system ever.'),
(10, 20, 4, 'Katniss is iconic. Great dystopian story.'),
(11, 15, 5, 'The magic system is incredibly creative.');

-- ============================================
-- 6. FOLLOWERS (32 follow relationships)
-- ============================================
INSERT INTO followers (follower_id, following_id) VALUES
-- Mutual follows
(2, 3), (3, 2),   -- Sarah and John
(2, 4), (4, 2),   -- Sarah and Emma
(2, 5), (5, 2),   -- Sarah and Michael
(3, 4), (4, 3),   -- John and Emma
(3, 9), (9, 3),   -- John and James
(4, 5), (5, 4),   -- Emma and Michael
(6, 7), (7, 6),   -- Lisa and David
(6, 8), (8, 6),   -- Lisa and Sophia
(7, 9), (9, 7),   -- David and James
(8, 10), (10, 8), -- Sophia and Olivia
(11, 12), (12, 11), -- William and Ava
(13, 14), (14, 13), -- Robert and Isabella
(15, 16), (16, 15), -- Daniel and Mia
(2, 10), (10, 2),   -- Sarah and Olivia
(3, 11), (11, 3),   -- John and William
(4, 12), (12, 4);   -- Emma and Ava

-- ============================================
-- 7. COMING_SOON_BOOKS (Sample upcoming releases)
-- ============================================
INSERT INTO coming_soon_books (title, author, genre, description, release_date, status, cover_image) VALUES
('The Winds of Winter', 'George R.R. Martin', 'Fantasy', 'Long-awaited sixth book in A Song of Ice and Fire series', '2024-12-01', 'UPCOMING', NULL),
('Untitled Harry Potter Sequel', 'J.K. Rowling', 'Fantasy', 'New adventure in the wizarding world', '2024-09-15', 'UPCOMING', NULL),
('Project Hail Mary 2', 'Andy Weir', 'Sci-Fi', 'Sequel to the bestselling science fiction novel', '2024-11-01', 'UPCOMING', NULL),
('The Last Devil to Die', 'Richard Osman', 'Mystery', 'The Thursday Murder Club returns for another case', '2024-08-20', 'UPCOMING', NULL),
('Chain-Gang All-Stars', 'Nana Kwame Adjei-Brenyah', 'Dystopian', 'A dystopian novel about prisoners fighting for freedom', '2024-10-05', 'UPCOMING', NULL),
('Yellowface', 'R.F. Kuang', 'Contemporary', 'A darkly comedic satire of the publishing industry', '2024-07-12', 'UPCOMING', NULL),
('Fourth Wing', 'Rebecca Yarros', 'Fantasy Romance', 'A dragon-riding academy romance', '2024-06-30', 'UPCOMING', NULL),
('The Covenant of Water', 'Abraham Verghese', 'Historical Fiction', 'A multi-generational family saga in India', '2024-08-01', 'UPCOMING', NULL);

-- ============================================
-- 8. APP_CHALLENGES (Community challenges)
-- ============================================
INSERT INTO app_challenges (name, description, challenge_type, target, required_genre, start_date, end_date, badge_name, badge_icon, is_active) VALUES
('Summer Reading Challenge 2024', 'Read 5 books during the summer season! Perfect for beach reads and lazy afternoons.', 'SEASONAL', 5, NULL, '2024-06-01', '2024-08-31', 'Summer Reader', '☀️', TRUE),
('Fantasy Explorer 2024', 'Dive deep into magical worlds and complete 3 fantasy novels this year.', 'GENRE', 3, 'Fantasy', '2024-01-01', '2024-12-31', 'Fantasy Master', '🧙', TRUE),
('Reading Marathon', 'Challenge yourself to read 10 books this year! Track your progress and earn badges.', 'READING_GOAL', 10, NULL, '2024-01-01', '2024-12-31', 'Marathon Runner', '🏃', TRUE),
('Mystery Month', 'Solve 2 mysteries this October! Perfect for spooky season.', 'EVENT', 2, 'Mystery', '2024-10-01', '2024-10-31', 'Mystery Solver', '🕵️', TRUE),
('Romance Reading Challenge', 'Fall in love with 4 romance novels throughout the year.', 'GENRE', 4, 'Romance', '2024-01-01', '2024-12-31', 'Romance Lover', '❤️', TRUE),
('Book Bingo 2024', 'Complete 5 different genres for a bingo!', 'EVENT', 5, NULL, '2024-04-01', '2024-12-31', 'Bingo Champion', '🏆', TRUE),
('Historical Fiction Journey', 'Travel through time with 3 historical fiction novels.', 'GENRE', 3, 'Historical Fiction', '2024-03-01', '2024-11-30', 'History Buff', '📜', TRUE),
('Sci-Fi Adventurer', 'Explore futuristic worlds with 3 science fiction books.', 'GENRE', 3, 'Sci-Fi', '2024-05-01', '2024-10-31', 'Space Explorer', '🚀', TRUE);

-- ============================================
-- 9. USER_APP_CHALLENGES (Users participating in challenges)
-- ============================================
INSERT INTO user_app_challenges (user_id, challenge_id, current_progress, completed, started_at, completed_at) VALUES
-- Sarah participates in multiple challenges
(2, 1, 2, FALSE, '2024-06-01 10:00:00', NULL),  -- Summer Reading (2/5)
(2, 2, 3, TRUE, '2024-01-15 09:30:00', '2024-05-20 14:00:00'),  -- Fantasy Explorer (completed)
(2, 3, 8, FALSE, '2024-01-05 11:00:00', NULL),  -- Reading Marathon (8/10)
-- John's challenges
(3, 1, 3, FALSE, '2024-06-02 08:00:00', NULL),  -- Summer Reading (3/5)
(3, 4, 0, FALSE, '2024-10-01 09:00:00', NULL),  -- Mystery Month (0/2)
-- Emma's challenges
(4, 2, 2, FALSE, '2024-02-01 10:30:00', NULL),  -- Fantasy Explorer (2/3)
(4, 5, 1, FALSE, '2024-03-15 14:00:00', NULL),  -- Romance Challenge (1/4)
-- Michael's challenges
(5, 3, 6, FALSE, '2024-01-10 15:00:00', NULL),  -- Reading Marathon (6/10)
(5, 8, 1, FALSE, '2024-05-02 11:00:00', NULL),  -- Sci-Fi Adventurer (1/3)
-- Lisa's challenges
(6, 5, 2, FALSE, '2024-02-20 09:00:00', NULL),  -- Romance Challenge (2/4)
(6, 6, 3, FALSE, '2024-04-01 10:00:00', NULL),  -- Book Bingo (3/5)
-- David's challenges
(7, 2, 1, FALSE, '2024-04-15 16:00:00', NULL),  -- Fantasy Explorer (1/3)
(7, 3, 9, FALSE, '2024-01-15 12:00:00', NULL),  -- Reading Marathon (9/10)
-- Sophia's challenges
(8, 1, 4, FALSE, '2024-06-05 14:00:00', NULL),  -- Summer Reading (4/5)
(8, 2, 2, FALSE, '2024-03-10 10:30:00', NULL),  -- Fantasy Explorer (2/3)
-- James' challenges
(9, 4, 1, FALSE, '2024-10-01 08:30:00', NULL),  -- Mystery Month (1/2)
(9, 7, 2, FALSE, '2024-03-05 15:00:00', NULL),  -- Historical Fiction (2/3)
-- Olivia's challenges
(10, 3, 7, FALSE, '2024-02-01 11:00:00', NULL),  -- Reading Marathon (7/10)
(10, 6, 4, FALSE, '2024-04-05 09:30:00', NULL); -- Book Bingo (4/5)

-- ============================================
-- 10. PERSONAL_CHALLENGES (User-created goals)
-- ============================================
INSERT INTO personal_challenges (user_id, title, description, target_books, start_date, end_date, is_completed) VALUES
(2, '2024 Reading Goal', 'Read 20 books this year for personal growth', 20, '2024-01-01', '2024-12-31', FALSE),
(2, 'Classic Literature', 'Read 5 classic novels I''ve been meaning to get to', 5, '2024-03-01', '2024-11-30', FALSE),
(3, 'Summer Vacation Reads', 'Read 3 books during my summer vacation', 3, '2024-07-01', '2024-08-31', FALSE),
(4, 'Mystery Marathon', 'Complete all Agatha Christie novels I own', 8, '2024-01-15', '2024-10-31', FALSE),
(5, 'Learn History Through Books', 'Read historical books from different time periods', 6, '2024-02-01', '2024-12-15', FALSE),
(6, 'Romantic Reads', 'Read one romance novel per month', 12, '2024-01-01', '2024-12-31', FALSE),
(7, 'Fantasy Series Completion', 'Finish 3 fantasy series I started', 3, '2024-03-01', '2024-11-30', FALSE),
(8, 'YA Fiction Exploration', 'Read popular YA novels to understand the genre', 10, '2024-04-01', '2024-12-31', FALSE),
(10, 'Book Club Books', 'Read all 6 book club selections this year', 6, '2024-01-01', '2024-12-31', FALSE),
(11, 'Science Fiction Classics', 'Read foundational sci-fi books', 5, '2024-05-01', '2024-10-31', FALSE);

-- ============================================
-- 11. PERSONAL_CHALLENGE_BOOKS (Books counted for personal challenges)
-- ============================================
INSERT INTO personal_challenge_books (challenge_id, book_id, added_at) VALUES
-- Sarah's 2024 Reading Goal (challenge_id = 1)
(1, 1, '2024-01-20 14:00:00'),
(1, 2, '2024-01-28 16:30:00'),
(1, 3, '2024-12-01 10:00:00'),
-- Sarah's Classic Literature (challenge_id = 2)
(2, 8, '2024-01-15 11:00:00'),
(2, 10, '2024-03-08 15:00:00'),
-- John's Summer Vacation Reads (challenge_id = 3)
(3, 16, '2024-11-15 09:00:00'),
-- Emma's Mystery Marathon (challenge_id = 4)
(4, 6, '2024-02-18 14:00:00'),
(4, 7, '2024-02-23 16:00:00'),
-- Michael's Learn History Through Books (challenge_id = 5)
(5, 8, '2024-01-15 12:00:00'),
-- Lisa's Romantic Reads (challenge_id = 6)
(6, 8, '2024-02-10 10:30:00'),
(6, 18, '2024-03-22 14:00:00'),
-- David's Fantasy Series Completion (challenge_id = 7)
(7, 3, '2024-02-20 16:00:00'),
-- Sophia's YA Fiction Exploration (challenge_id = 8)
(8, 1, '2024-05-07 11:00:00'),
(8, 2, '2024-05-15 15:00:00'),
-- Olivia's Book Club Books (challenge_id = 9)
(9, 19, '2024-08-10 13:00:00'),
-- William's Science Fiction Classics (challenge_id = 10)
(10, 14, '2024-05-30 17:00:00');

-- ============================================
-- 12. UPDATE BOOK COVERS
-- Resetting image URLs for consistency
-- ============================================

-- 1. Keep ONLY Game of Thrones with custom cover
UPDATE books SET cover_image_url = 'uploads/books/game-of-thrones.jpg' 
WHERE title LIKE '%Game of Thrones%';

-- 2. Set ALL other books to NULL (will use default cover)
UPDATE books SET cover_image_url = NULL 
WHERE title NOT LIKE '%Game of Thrones%';

-- 3. Cleanup coming_soon_books
UPDATE coming_soon_books SET cover_image = NULL;
