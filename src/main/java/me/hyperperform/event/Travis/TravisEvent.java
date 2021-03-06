package me.hyperperform.event.Travis;

import me.hyperperform.event.IEvent;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Travis Event POJO which contains build information
 * Contains Annotations for Persistence that stores the data via JPA
 *
 * @author  CodusMaximus
 * @version 1.0
 * @since   2016/08/03
 */


@Entity
@Table(name = "\"TravisEvent\"")
public class TravisEvent implements IEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private int id;

    @Column(name = "commiter")
    private String commiter;

    @Column(name = "branch")
    private String branch;

    @Column(name = "repo")
    private String repo;

    @Column(name = "status")
    private String status;

    @Column(name = "timestamp")
    private Timestamp timestamp;

    public TravisEvent() {
    }

    /**
     * Public constructor that allows for quick creation of POJO.
     * @param repo Repository that is being built
     * @param branch Which branch in particular was the build instantiated
     * @param commiter Who initiated the build
     * @param status What was the outcome of the build i.e passed, failed
     * @param timestamp When did this event take place
     */
    public TravisEvent(String repo, String branch, String commiter, String status, String timestamp) {
        this.repo = repo;
        this.branch = branch;
        this.commiter = commiter;
        this.status = status;
        this.timestamp = Timestamp.valueOf(timestamp);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCommiter() {
        return commiter;
    }

    public void setCommiter(String commiter) {
        this.commiter = commiter;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getRepo() {
        return repo;
    }

    public void setRepo(String repo) {
        this.repo = repo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimestamp() {
        return timestamp.toString();
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = Timestamp.valueOf(timestamp);
    }

    public String toString()
    {
        String s = "";

        s += "Commiter: " + commiter + "\n";
        s += "Repo: " + repo + "\n";
        s += "Branch: " + branch + "\n";
        s += "Status: " + status + "\n";
        s += "Timestamp: " + timestamp + "\n";

        return s;
    }
}
