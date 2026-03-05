package gr.aueb.cf.eduapp.runner;

import gr.aueb.cf.eduapp.service.IEligibleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class EligibleResultsRunner implements CommandLineRunner {

    private final IEligibleService eligibleService;

    // starts ./gradlew bootRun --args='--generate-report'
    @Override
    public void run(String... args) throws Exception {
        String jobId = UUID.randomUUID().toString();

        if (args.length > 0 && args[0].equals("--generate-report")) {
            log.info("Starting job with id={}", jobId);
            eligibleService.generateReport(jobId);  // fires async, app continues starting
        } else if (args.length > 0 && args[0].equals("--get-status")) {
            log.info("Get job status with job id={}", jobId);
            eligibleService.getJobStatus(jobId);
        }
    }
}
