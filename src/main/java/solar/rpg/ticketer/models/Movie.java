package solar.rpg.ticketer.models;

/**
 * Represents the 'Movie' model in the architecture.
 * A movie is a short film that can be viewed at a screening.
 * All of its attributes are defined in this class.
 *
 * @author Joshua Skinner
 * @version 1.0
 * @see Screening
 * @since 0.1
 */
public class Movie {

    private final String name, genre;
    private final int runningTime, releaseYear;

    public Movie(String name, String genre, int runningTime, int releaseYear) {
        this.name = name;
        this.genre = genre;
        this.runningTime = runningTime;
        this.releaseYear = releaseYear;
    }

    /**
     * @return The name, or title, of this movie.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The genre, or theme, of this movie.
     */
    public String getGenre() {
        return genre;
    }

    /**
     * @return The running time of this movie, in minutes.
     */
    public int getRunningTime() {
        return runningTime;
    }

    /**
     * @return The year this movie was released.
     */
    public int getReleaseYear() {
        return releaseYear;
    }

    @Override
    public boolean equals(Object obj) {
        // Movies are equal if they share the same title.
        if (!(obj instanceof Movie)) return false;
        if (obj == this) return true;
        return ((Movie) obj).name.equals(this.name);
    }
}
