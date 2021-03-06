package me.hyperperform.reporting;

import me.hyperperform.event.EntryExit.AccessEvent;
import me.hyperperform.event.Git.GitIssue;
import me.hyperperform.event.Git.GitPush;
import me.hyperperform.event.Travis.TravisEvent;
import me.hyperperform.forecasting.IForecasting;
import me.hyperperform.forecasting.request.GetForecastTimeRequest;
import me.hyperperform.forecasting.request.GetForecastValueRequest;
import me.hyperperform.reporting.algorithm.Algorithm;
import me.hyperperform.reporting.algorithm.MultimediaAlgorithm;
import me.hyperperform.reporting.algorithm.StandardAlgorithm;
import me.hyperperform.reporting.algorithm.WebDeveloperAlgorithm;
import me.hyperperform.reporting.request.*;
import me.hyperperform.reporting.response.*;
import me.hyperperform.user.EmployeeRole;
import me.hyperperform.user.Position;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.inject.Inject;
import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A realization of the {@see me.hyperperform.reporting.IReport} interface. All three contracts defined in IReport have
 * been met. Report generators can be easily plugged in and out as long as the contract is met.
 *
 * @author : CodusMaximus
 * @version : 1.0
 * @since : 2016/08/10
 */
public class ReportGenerator implements IReport {
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @Inject
    IForecasting forecasting;

    @PostConstruct
    private void initConnection() {
        entityManagerFactory = Persistence.createEntityManagerFactory("PostgreJPA");
        entityManager = entityManagerFactory.createEntityManager();
    }

    @PreDestroy
    private void disconnect() {
        entityManager.close();
        entityManagerFactory.close();
    }

    public ReportGenerator() {
    }

    /**
     * Generates a summary for all the event data received within a specific time span. Summaries are generated for
     * each integration which currently exist. Each summary is independent from the others.
     *
     * @param getSummaryRequest Request object which contains the necessary details for the summary, this includes the
     *                          name of the user for whom the summary will be for as well as a time period.
     * @return The response object contains the summaries for each individual integration.
     */
    public GetSummaryResponse getSummary(GetSummaryRequest getSummaryRequest) {
        GetSummaryResponse getSummaryResponse = new GetSummaryResponse();

        /*-------------------Mapping Email to name----------------------*/
        Query q = entityManager.createQuery("SELECT a.gitUserName FROM User a WHERE userEmail=:email").setParameter("email", getSummaryRequest.getName());
//        getSummaryRequest.setName((String)q.getSingleResult());
        String gitUserName = (String) q.getSingleResult();
        /*--------------------------------------------------------------*/

        /*---------------------------Github-----------------------------*/
        q = entityManager.createQuery("SELECT sum(a.commitSize) FROM GitPush a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (username=:uname)").setParameter("startDate", getSummaryRequest.getStartDate()).setParameter("endDate", getSummaryRequest.getEndDate()).setParameter("uname", gitUserName);

//        long time = TimeUnit.MILLISECONDS.toDays(getSummaryRequest.getEndDate().getTime() - getSummaryRequest.getStartDate().getTime());
        Long totalCommits = (Long) q.getSingleResult();

//        if (totalCommits != null)
//        {
//            GetForecastTimeRequest getForecastTimeRequest = new GetForecastTimeRequest("GitCommits", getPosition(getSummaryRequest.getName()));
//            time = convertDays(time, forecasting.getForecastTime(getForecastTimeRequest).getTime());
//
//            GetForecastValueRequest getForecastValueRequest = new GetForecastValueRequest("GitCommits", getPosition(getSummaryRequest.getName()));
//            double forecastValue = forecasting.getForecastValue(getForecastValueRequest).getValue();
//
//            double avg = (double) totalCommits / (double) time;
//            avg /= forecastValue;
//            long tmp = (long) (avg * 10000.0);
//
//            getSummaryResponse.setGithub((double) (tmp) / 100.0);
//        }

          getSummaryResponse.setGithub( totalCommits == null ? 0 : totalCommits);
        /*--------------------------------------------------------------*/

        /*----------------------------Travis-----------------------------*/
        q = entityManager.createQuery("SELECT COUNT(a.status) FROM TravisEvent a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (commiter=:uname) AND (status LIKE 'Passed')").setParameter("startDate", getSummaryRequest.getStartDate()).setParameter("endDate", getSummaryRequest.getEndDate()).setParameter("uname", gitUserName);
        long passed = (Long) q.getSingleResult();

        q = entityManager.createQuery("SELECT COUNT(a.status) FROM TravisEvent a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (commiter=:uname) AND (status LIKE 'Failed')").setParameter("startDate", getSummaryRequest.getStartDate()).setParameter("endDate", getSummaryRequest.getEndDate()).setParameter("uname", gitUserName);
        long failed = (Long) q.getSingleResult();

        double successRate = ((double) passed / (double) (passed + failed)) * 100.0;
        int roundTmp = (int) (successRate * 100.0);
        successRate = roundTmp / 100.0;

        getSummaryResponse.setTravis(successRate);
        /*--------------------------------------------------------------*/

        /*--------------------------Bug Tracking------------------------*/
        q = entityManager.createQuery("SELECT COUNT(a.action) FROM GitIssue a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (assignee=:uname) AND (action LIKE 'assigned')").setParameter("startDate", getSummaryRequest.getStartDate()).setParameter("endDate", getSummaryRequest.getEndDate()).setParameter("uname", gitUserName);
        Long assigned = (Long) q.getSingleResult();

        q = entityManager.createQuery("SELECT COUNT(a.action) FROM GitIssue a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (assignee=:uname) AND (action LIKE 'closed')").setParameter("startDate", getSummaryRequest.getStartDate()).setParameter("endDate", getSummaryRequest.getEndDate()).setParameter("uname", gitUserName);
        Long closed = (Long) q.getSingleResult();
//
//        double closeRate = ((double) closed / (double) assigned) * 100.0;
//        int roundTmp2 = (int) (closeRate * 100.0);
//        closeRate = roundTmp2 / 100.0;
        double ratio = ( (assigned == null) || (assigned == 0) ) ? 0 : (((double)closed/(double)assigned) * 100.0);
//        System.out.println("\n\n" +assigned + "\n\n" + closed + "\n\n" + ratio);
        getSummaryResponse.setIssues(ratio );
        /*--------------------------------------------------------------*/

        /*--------------------------Entry Exit------------------------*/
        q = entityManager.createQuery("SELECT a FROM AccessEvent a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (email=:uname)").setParameter("startDate", getSummaryRequest.getStartDate()).setParameter("endDate", getSummaryRequest.getEndDate()).setParameter("uname", getSummaryRequest.getName());
        List<AccessEvent> list = q.getResultList();

        long val = 0;
        for (int k = 0; k < list.size(); k += 2) {
            Timestamp a = Timestamp.valueOf(list.get(k).getTimestamp());
            Timestamp b = Timestamp.valueOf(list.get((k + 1 < list.size()) ? k + 1 : k).getTimestamp());

            val += TimeUnit.MILLISECONDS.toHours(b.getTime() - a.getTime());
        }

//        Long loghours = (Long) q.getSingleResult();
//        double hours = 0;
//        if (loghours != null)
//        {
//            GetForecastTimeRequest getForecastTimeRequest = new GetForecastTimeRequest("EntryExit", getPosition(getSummaryRequest.getName()));
//            time = convertDays(time, forecasting.getForecastTime(getForecastTimeRequest).getTime());
//
//            GetForecastValueRequest getForecastValueRequest = new GetForecastValueRequest("EntryExit", getPosition(getSummaryRequest.getName()));
//            double forecastValue = forecasting.getForecastValue(getForecastValueRequest).getValue();
//
//
//            double avg = (double) loghours / (double) time;
//            avg /= forecastValue;
//            long tmp = (long) (avg * 10000.0);
//            hours = (double) tmp / 100.0;
//            System.out.println("\n\n LOG: " + tmp + "\n\n" + "Email: " + getSummaryRequest.getName());
//        }
        getSummaryResponse.setEntryExit( val);
        /*--------------------------------------------------------------*/

        return getSummaryResponse;
    }

    /**
     * Generates a detailed report for a specified integration. These reports also require a user and time span.
     * The type of integration for which the detailed report is required must be specified every time a request is made.
     *
     * @param getDetailsRequest Request object which contains the necessary details for the summary, this includes the
     *                          name of the user for whom the summary will be for as well as a time period.
     * @return Returns an object which contains detailed information for a specific integration.
     */
    public GetDetailsResponse getDetails(GetDetailsRequest getDetailsRequest) {

        /*-------------------Mapping Email to name----------------------*/
        Query z = entityManager.createQuery("SELECT a.gitUserName FROM User a WHERE userEmail=:email").setParameter("email", getDetailsRequest.getName());
//        getDetailsRequest.setName((String)z.getSingleResult());
        String gitUserName = (String) z.getSingleResult();
        /*--------------------------------------------------------------*/

        GetDetailsResponse getDetailsResponse = new GetDetailsResponse();

        if (getDetailsRequest.getType().equals("travis")) {
            System.out.println("------------------------------------------------");
            System.out.println("Generating report for travis: " + gitUserName);
            System.out.println("Start: " + getDetailsRequest.getStartDate());
            System.out.println("End: " + getDetailsRequest.getEndDate());
            System.out.println("------------------------------------------------");

            Query q = entityManager.createQuery("SELECT a FROM TravisEvent a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (commiter=:uname)").setParameter("startDate", getDetailsRequest.getStartDate()).setParameter("endDate", getDetailsRequest.getEndDate()).setParameter("uname", gitUserName);
            List<TravisEvent> result = q.getResultList();

            ArrayList<String> repos = new ArrayList<String>();
            ArrayList<ArrayList<TravisEvent>> data = new ArrayList<ArrayList<TravisEvent>>();
            ArrayList<Double[]> graphData = new ArrayList<Double[]>();

            for (int k = 0; k < result.size(); k++) {
                TravisEvent curr = result.get(k);

                if (repos.indexOf(curr.getRepo()) == -1) {
                    repos.add(curr.getRepo());
                    data.add(new ArrayList<TravisEvent>());
                }

                data.get(repos.indexOf(curr.getRepo())).add(curr);
            }

            for (int k = 0; k < repos.size(); k++)
            {
                q = entityManager.createQuery("SELECT a FROM TravisEvent a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (commiter=:uname) AND (repo=:repoName))")
                        .setParameter("startDate", getDetailsRequest.getStartDate())
                        .setParameter("endDate", getDetailsRequest.getEndDate())
                        .setParameter("uname", gitUserName)
                        .setParameter("repoName", repos.get(k));

                double passed = 0;
                double failed = 0;

                List<TravisEvent> tmp = q.getResultList();
                for (int i = 0; i < tmp.size(); i++)
                {
                    if (tmp.get(i).getStatus().equals("Passed"))
                        passed++;
                    else
                        failed++;
                }

                graphData.add(new Double[]{passed, failed});
            }

            getDetailsResponse.setTravisDetails(new TravisDetails(data.size(), data, graphData));

        } else if (getDetailsRequest.getType().equals("git")) {
            System.out.println("------------------------------------------------");
            System.out.println("Generating report for Git: " + gitUserName);
            System.out.println("Start: " + getDetailsRequest.getStartDate());
            System.out.println("End: " + getDetailsRequest.getEndDate());
            System.out.println("------------------------------------------------");


            Query q = entityManager.createQuery("SELECT a FROM GitPush a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (username=:uname)").setParameter("startDate", getDetailsRequest.getStartDate()).setParameter("endDate", getDetailsRequest.getEndDate()).setParameter("uname", gitUserName);
            List<GitPush> result = q.getResultList();

            ArrayList<String> repos = new ArrayList<String>();
            ArrayList<ArrayList<GitPush>> data = new ArrayList<ArrayList<GitPush>>();

            for (int k = 0; k < result.size(); k++) {
                GitPush curr = result.get(k);

                if (repos.indexOf(curr.getRepository()) == -1) {
                    repos.add(curr.getRepository());
                    data.add(new ArrayList<GitPush>());
                }

                data.get(repos.indexOf(curr.getRepository())).add(curr);
            }


            long range = (getDetailsRequest.getEndDate().getTime() - getDetailsRequest.getStartDate().getTime());
            range /= 10;

            ArrayList<GraphData<String, Long>> graphData = new ArrayList<GraphData<String, Long>>();

            for (int k = 0; k < repos.size(); k++) {
                ArrayList<String> xAxis = new ArrayList<String>();
                ArrayList<Long> yAxis = new ArrayList<Long>();

                long prev = getDetailsRequest.getStartDate().getTime();
                for (int j = 1; j <= 10; j++) {
                    long curr = prev + range;

                    Query dataQuery = entityManager.createQuery("SELECT SUM(a.commitSize) FROM GitPush a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (username=:uname) AND (repository=:repo)")
                            .setParameter("startDate", new Timestamp(prev))
                            .setParameter("endDate", new Timestamp(curr))
                            .setParameter("uname", gitUserName)
                            .setParameter("repo", repos.get(k));

                    Object commits = dataQuery.getSingleResult();

                    String currXLabel = (new Timestamp(curr)).toString();

                    xAxis.add(currXLabel.substring(0, currXLabel.indexOf(" ")));
                    yAxis.add((commits == null) ? 0 : (Long) commits);

                    prev = curr;
                }

                graphData.add(new GraphData<String, Long>(xAxis, yAxis));
            }

            q = entityManager.createQuery("SELECT sum(a.commitSize) FROM GitPush a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (username=:uname)").setParameter("startDate", getDetailsRequest.getStartDate()).setParameter("endDate", getDetailsRequest.getEndDate()).setParameter("uname", gitUserName);
            Long totalCommits = (Long) q.getSingleResult();

            getDetailsResponse.setGitDetails(new GitDetails(data.size(), data, graphData, (totalCommits == null) ? 0 : totalCommits));

        } else if (getDetailsRequest.getType().equals("issues")) {
            System.out.println("------------------------------------------------");
            System.out.println("Generating report for Issues: " + gitUserName);
            System.out.println("Start: " + getDetailsRequest.getStartDate());
            System.out.println("End: " + getDetailsRequest.getEndDate());
            System.out.println("------------------------------------------------");


            Query q = entityManager.createQuery("SELECT a FROM GitIssue a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (assignee=:uname) AND (action='closed' OR action='assigned')").setParameter("startDate", getDetailsRequest.getStartDate()).setParameter("endDate", getDetailsRequest.getEndDate()).setParameter("uname", gitUserName);
            List<GitIssue> result = q.getResultList();

            ArrayList<String> repos = new ArrayList<String>();
            ArrayList<ArrayList<GitIssue>> data = new ArrayList<ArrayList<GitIssue>>();

            for (int k = 0; k < result.size(); k++) {
                GitIssue curr = result.get(k);

                if (repos.indexOf(curr.getRepository()) == -1) {
                    repos.add(curr.getRepository());
                    data.add(new ArrayList<GitIssue>());
                }

                data.get(repos.indexOf(curr.getRepository())).add(curr);
            }

            getDetailsResponse.setGitIssueDetails(new GitIssueDetails(data.size(), data));
        } else if (getDetailsRequest.getType().equals("entry")) {
            System.out.println("------------------------------------------------");
            System.out.println("Generating report for Entry: " + getDetailsRequest.getName());
            System.out.println("Start: " + getDetailsRequest.getStartDate());
            System.out.println("End: " + getDetailsRequest.getEndDate());
            System.out.println("------------------------------------------------");


            Query q = entityManager.createQuery("SELECT a FROM AccessEvent a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (email=:uname) order by timestamp").setParameter("startDate", getDetailsRequest.getStartDate()).setParameter("endDate", getDetailsRequest.getEndDate()).setParameter("uname", getDetailsRequest.getName());
            List<AccessEvent> result = q.getResultList();

            long range = (getDetailsRequest.getEndDate().getTime() - getDetailsRequest.getStartDate().getTime());
            range /= 10;

            GraphData<String, Long> graphData = new GraphData<String, Long>();

            ArrayList<String> xAxis = new ArrayList<String>();
            ArrayList<Long> yAxis = new ArrayList<Long>();

            long prev = getDetailsRequest.getStartDate().getTime();
            for (int j = 1; j <= 10; j++) {
                long curr = prev + range;

                Query dataQuery = entityManager.createQuery("SELECT a FROM AccessEvent a WHERE (timestamp BETWEEN :startDate AND :endDate) AND (email=:uname) order by timestamp")
                        .setParameter("startDate", new Timestamp(prev))
                        .setParameter("endDate", new Timestamp(curr))
                        .setParameter("uname", getDetailsRequest.getName());

                String currXLabel = (new Timestamp(curr)).toString();

                List<AccessEvent> list = dataQuery.getResultList();

                long val = 0;
                for (int k = 0; k < list.size(); k += 2) {
                    Timestamp a = Timestamp.valueOf(list.get(k).getTimestamp());
                    Timestamp b = Timestamp.valueOf(list.get((k + 1 < list.size()) ? k + 1 : k).getTimestamp());

                    val += TimeUnit.MILLISECONDS.toHours(b.getTime() - a.getTime());
                }

                xAxis.add(currXLabel.substring(0, currXLabel.indexOf(" ")));
                yAxis.add(val);

                prev = curr;
            }

            graphData.setIndependent(xAxis);
            graphData.setDependent(yAxis);

            getDetailsResponse.setAccessDetails(new AccessDetails((ArrayList<AccessEvent>) result, graphData));
        }

        return getDetailsResponse;
    }

    /**
     * Generates a performance score for a specified user. Score generation also requires a user and time span.
     * Each score is generated using an {@see me.hyperperform.reporting.algorithm.Algorithm}.
     *
     * @param getScoreRequest Request object which contains the necessary details for the summary, this includes the
     *                        name of the user for whom the score will be for as well as a time period.
     * @return Returns an object which contains the performance score for a particular employee.
     */
    public GetScoreResponse getScore(GetScoreRequest getScoreRequest) {


        CalculateScoreRequest calculateScoreRequest = new CalculateScoreRequest();
        calculateScoreRequest.setName(getScoreRequest.getName());
        calculateScoreRequest.setStartDate(getScoreRequest.getStartDate());
        calculateScoreRequest.setEndDate(getScoreRequest.getEndDate());

        Position userPosition = getPosition(getScoreRequest.getName());
        Algorithm algorithm = null;

        if (userPosition == Position.SoftwareDeveloper)
            algorithm = new StandardAlgorithm();

        if (userPosition == Position.WebDeveloper)
            algorithm = new WebDeveloperAlgorithm();

        if (userPosition == Position.Multimedia)
            algorithm = new MultimediaAlgorithm();

        CalculateScoreResponse calculateScoreResponse = algorithm.calculateScore(calculateScoreRequest);

        String performance = "Non-performer";

        double score = calculateScoreResponse.getScore();
        if (score >= 2.0 && score < 3.0)
            performance = "Standard performer";
        else if (score >= 3.0 && score < 4.0)
            performance = "Standard plus performer";
        else if (score >= 4.0)
            performance = "High performer";

        return new GetScoreResponse(score, performance);
    }

    private Position getPosition(String user) {
        Query q = entityManager.createQuery("select position from User where userEmail=:email").setParameter("email", user);
        Position p = (Position) q.getSingleResult();

        return (p == null) ? null : p;
    }

    private long convertDays(long days, String time)
    {
        if (time.equals("week"))
        {
            return days/7;
        }

        return days;
    }
}
