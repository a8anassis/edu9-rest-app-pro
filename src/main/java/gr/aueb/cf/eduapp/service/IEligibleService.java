package gr.aueb.cf.eduapp.service;

import gr.aueb.cf.eduapp.core.exceptions.EntityNotFoundException;
import gr.aueb.cf.eduapp.dto.JobStatusDTO;

public interface IEligibleService {
    void generateReport(String jobId);
    JobStatusDTO getJobStatus(String jobId);
}
