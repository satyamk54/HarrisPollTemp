package ca.adintel;

public class SourceForwarder {
    public String targetMediaTempFolder;
    public String generalOrHispanic;
    public String sftpTargetPath;
    public String emailSubjectMediaPartMatch;
    public Pair<String> storedRequests = new Pair<>();
    public Pair<String> downloadedFileNames = new Pair<>();

    public SourceForwarder(final String targetMediaTempFolder, final String generalOrHispanic, final String sftpTargetPath, final String emailSubjectMediaPartMatch) {
        this.targetMediaTempFolder = targetMediaTempFolder;
        this.generalOrHispanic = generalOrHispanic;
        this.sftpTargetPath = sftpTargetPath;
        this.emailSubjectMediaPartMatch = emailSubjectMediaPartMatch;
    }

    public boolean subjectMatch(String subject) {
        if (!subject.toLowerCase().contains(generalOrHispanic.toLowerCase())) return false;
        return subject.toLowerCase().contains(emailSubjectMediaPartMatch.toLowerCase());

    }
}
