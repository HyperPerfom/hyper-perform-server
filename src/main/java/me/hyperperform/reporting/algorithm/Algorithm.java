package me.hyperperform.reporting.algorithm;

import me.hyperperform.reporting.request.CalculateScoreRequest;
import me.hyperperform.reporting.response.CalculateScoreResponse;

/**
 * Created by rohan on 2016/08/19.
 */
public interface Algorithm {

    CalculateScoreResponse calculateScore(CalculateScoreRequest calculateScoreRequest);

}
