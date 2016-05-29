package ru.asartamonov.ohsinner;

/**
 * Alexander Artamonov (asartamonov@gmail.com) 2016.
 */
public class Path {

    private int pathID;
    private String pathDescription;
    private final boolean isApproved;

    private Path(int pathID, String pathDescription, boolean isApproved) {
        this.pathID = pathID;
        this.pathDescription = pathDescription;
        this.isApproved = isApproved;
    }

    public int getPathID() {
        return pathID;
    }

    public String getPathDescription() {
        return pathDescription;
    }

    public void setPathDescription(String pathDescription) {
        this.pathDescription = pathDescription;
    }

    public boolean isApproved() {
        return isApproved;
    }

    static class PathManager {
        public static Path newInapprovedPath(int pathID, String pathDescription) {
            //creation logic, dbManager calls
            return new Path(pathID, pathDescription, false);
        }

        public static Path newApprovedPath(int pathID, String pathDescription) {
            //creation logic, dbManager calls
            return new Path(pathID, pathDescription, true);
        }

        public static void syncPathWithDB(Sin sin, Path path) {
            if (path.getPathID() != 0)
                DbManager.setPathDescription(path.getPathDescription(), path.getPathID());
            else {
                int pathID = DbManager.createPath(path.getPathDescription(), false);
                DbManager.setSinPath(pathID, sin.getSinID());
            }
        }
    }
}