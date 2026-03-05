package gr.aueb.cf.eduapp.service;

import gr.aueb.cf.eduapp.dto.JobStatusDTO;
import gr.aueb.cf.eduapp.dto.TeacherStatusReportView;
import gr.aueb.cf.eduapp.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class EligibleService implements IEligibleService {
    private final Map<String, JobStatusDTO> jobStatusMap = new ConcurrentHashMap<>();
    private final TeacherRepository teacherRepository;

    @Async
    @Transactional(readOnly = true)
    public void generateReport(String jobId) {
        jobStatusMap.put(jobId, JobStatusDTO.withoutData(jobId, "IN_PROGRESS"));

        try {
            // Execute the query
            List<TeacherStatusReportView> report = teacherRepository.findAllTeachersReport();

            // Store the result alongside the status
            jobStatusMap.put(jobId, new JobStatusDTO(jobId, "COMPLETED", report));
            log.info("Report generated for jobId={}, records={}", jobId, report.size());
        } catch (Exception e) {
            jobStatusMap.put(jobId, JobStatusDTO.withoutData(jobId, "FAILED"));
            log.error("Failed to generate report for jobId={}", jobId, e);
            throw new RuntimeException(e);  // wrap it
        }
    }

    @Override
    public JobStatusDTO getJobStatus(String jobId) {
        return jobStatusMap.get(jobId); // returns null if jobId not found
    }
}
