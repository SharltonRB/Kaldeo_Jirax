package com.issuetracker.dto;

import java.util.List;

/**
 * Response DTO for sprint activation containing the updated sprint and affected issues.
 */
public class SprintActivationResponse {
    
    private SprintDto sprint;
    private List<Long> updatedIssueIds;
    private int movedIssuesCount;
    
    public SprintActivationResponse() {}
    
    public SprintActivationResponse(SprintDto sprint, List<Long> updatedIssueIds, int movedIssuesCount) {
        this.sprint = sprint;
        this.updatedIssueIds = updatedIssueIds;
        this.movedIssuesCount = movedIssuesCount;
    }
    
    public SprintDto getSprint() {
        return sprint;
    }
    
    public void setSprint(SprintDto sprint) {
        this.sprint = sprint;
    }
    
    public List<Long> getUpdatedIssueIds() {
        return updatedIssueIds;
    }
    
    public void setUpdatedIssueIds(List<Long> updatedIssueIds) {
        this.updatedIssueIds = updatedIssueIds;
    }
    
    public int getMovedIssuesCount() {
        return movedIssuesCount;
    }
    
    public void setMovedIssuesCount(int movedIssuesCount) {
        this.movedIssuesCount = movedIssuesCount;
    }
    
    @Override
    public String toString() {
        return "SprintActivationResponse{" +
                "sprint=" + sprint +
                ", updatedIssueIds=" + updatedIssueIds +
                ", movedIssuesCount=" + movedIssuesCount +
                '}';
    }
}