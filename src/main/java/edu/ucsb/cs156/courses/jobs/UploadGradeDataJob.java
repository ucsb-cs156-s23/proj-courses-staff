package edu.ucsb.cs156.courses.jobs;

import java.util.List;

import org.hibernate.JDBCException;
import org.hibernate.exception.ConstraintViolationException;

import edu.ucsb.cs156.courses.entities.GradeHistory;
import edu.ucsb.cs156.courses.repositories.GradeHistoryRepository;
import edu.ucsb.cs156.courses.services.UCSBGradeHistoryService;
import edu.ucsb.cs156.courses.services.jobs.JobContext;
import edu.ucsb.cs156.courses.services.jobs.JobContextConsumer;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class UploadGradeDataJob implements JobContextConsumer {
    @Getter
    private UCSBGradeHistoryService ucsbGradeHistoryService;
    @Getter
    private GradeHistoryRepository gradeHistoryRepository;

    @Override
    public void accept(JobContext ctx) throws Exception {
        ctx.log("Updating UCSB Grade History Data");
        List<String> urls = ucsbGradeHistoryService.getUrls();

        GradeHistory previous = new GradeHistory();
        int count = 0;
        List<GradeHistory> results = null;
        for (String url : urls) {
            try {
                results = ucsbGradeHistoryService.getGradeData(url);
                GradeHistory topRow = results.get(0);
                gradeHistoryRepository.upsertAll(results);
                count++;
                logProgress(ctx, topRow, previous, count);
            } catch (Exception e) {
                ctx.log("Exception processing url: " + url);
                ctx.log("results: " + results);
                ctx.log(e.toString());
                throw e;
            }
        }

        ctx.log("Finished updating UCSB Grade History Data");
    }

    private void logProgress(JobContext ctx, GradeHistory topRow, GradeHistory previous, int count) {
        if (!topRow.getYear().equals(previous.getYear())) {
            ctx.log("Processing data for year: " + topRow.getYear());
            previous.setYear(topRow.getYear());
        }
        if (!topRow.getSubjectArea().equals(previous.getSubjectArea())) {
            ctx.log("Processing data for subjectArea: " + topRow.getSubjectArea());
            previous.setSubjectArea(topRow.getSubjectArea());
        }
        if (count % 100 == 0) {
            ctx.log("Processed " + count + " courses");
        }
    }
}