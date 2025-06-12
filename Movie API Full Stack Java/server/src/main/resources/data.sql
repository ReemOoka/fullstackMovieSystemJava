-- Initial movie data - using INSERT IGNORE to prevent errors if data already exists
INSERT IGNORE INTO movies (id, title, director, release_year, genre) VALUES
(1, 'Inception', 'Christopher Nolan', 2010, 'Sci-Fi'),
(2, 'The Shawshank Redemption', 'Frank Darabont', 1994, 'Drama'),
(3, 'The Dark Knight', 'Christopher Nolan', 2008, 'Action'),
(4, 'Pulp Fiction', 'Quentin Tarantino', 1994, 'Crime'),
(5, 'Forrest Gump', 'Robert Zemeckis', 1994, 'Drama');

