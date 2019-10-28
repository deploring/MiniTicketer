CREATE SCHEMA MiniTicketer; -- This only needs to be done once.
USE MiniTicketer;

-- WARNING: Checks will be parsed but not enforced in versions of MySQL < 8.0!!

CREATE TABLE Genre (
	`genre` CHAR(20) NOT NULL, -- Denotes name of genre.
	PRIMARY KEY(`genre`)
);

CREATE TABLE Movie (
	`name` CHAR(60) NOT NULL, -- Denotes name of movie.
	`running_time` SMALLINT UNSIGNED NOT NULL, -- Denotes running time of movie, in minutes.
	`release_year` SMALLINT UNSIGNED NOT NULL, -- Denotes release year of movie.
	`genre` CHAR(20) NOT NULL, -- Denotes genre of movie.
	CHECK (`running_time` > 0), -- Running time must be greater than zero.
	CHECK (`release_year` > 1900), -- Running time must be greater than zero.
	PRIMARY KEY(`name`),
	FOREIGN KEY (`genre`) REFERENCES Genre(`genre`) -- FK1
);

CREATE TABLE Venue (
	`venue_no` SMALLINT UNSIGNED NOT NULL, -- Venue number, e.g. Venue 17
	`no_of_rows` TINYINT UNSIGNED NOT NULL DEFAULT 18, -- Number of seat rows at venue.
	`no_of_cols` TINYINT UNSIGNED NOT NULL DEFAULT 30, -- Number of seat columns at venue.
	CHECK(`no_of_rows` > 0), -- Venue must have at least 1 seat row.
	CHECK(`no_of_cols` > 0), -- Venue must have at least 1 seat column.
	CHECK(`venue_no` > 0), -- Venue number cannot be zero.
	PRIMARY KEY(`venue_no`)
);

CREATE TABLE Screening (
	`start_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Start of date range for movie screening.
	`end_date` TIMESTAMP NOT NULL, -- End of date range for movie screening.
	`movie_name` CHAR(60) NOT NULL, -- Name of movie that is being screened.
	`screening_id` INT UNSIGNED NOT NULL UNIQUE AUTO_INCREMENT, -- Unique number identifier for this screening.
	`venue_no` SMALLINT UNSIGNED NOT NULL, -- Venue that the screening is being held at.
	CHECK(`start_date` < `end_date`), -- Start date must always come before end date.
	PRIMARY KEY(`start_date`, `end_date`, `movie_name`),
	FOREIGN KEY (`movie_name`) REFERENCES Movie (`name`), -- FK1
	FOREIGN KEY (`venue_no`) REFERENCES Venue (`venue_no`) -- FK2
);

CREATE TABLE Screening_Times (
	`screening_id` INT UNSIGNED NOT NULL, -- Screening that this time slot applies to.
	`screening_time` CHAR(5) NOT NULL, -- A specified time slot for this screening.
	`screening_day` CHAR(9) NOT NULL, -- A specified available day for this screening.
	PRIMARY KEY(`screening_id`, `screening_time`, `screening_day`),
	FOREIGN KEY (`screening_id`) REFERENCES Screening(`screening_id`) -- FK1
);

CREATE TABLE Ticket (
	`screening_id` INT UNSIGNED NOT NULL, -- Screening that this ticket has a booking with.
	`selected_date` TIMESTAMP NOT NULL, -- Selected screening date.
	`allocated_seat` CHAR(3) NOT NULL, -- Allocated seat number.
	`username` CHAR(16) NOT NULL, -- Username of booking holder.
	PRIMARY KEY(`screening_id`, `selected_date`, `allocated_seat`),
	FOREIGN KEY (`screening_id`) REFERENCES Screening(`screening_id`) -- FK1
);