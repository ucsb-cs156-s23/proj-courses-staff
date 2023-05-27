package edu.ucsb.cs156.courses.controllers;

import edu.ucsb.cs156.courses.entities.GradeHistory;

import edu.ucsb.cs156.courses.repositories.GradeHistoryRepository;
import edu.ucsb.cs156.courses.services.UCSBGradeHistoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

@Slf4j
@Api(description = "API for grade history data")
@RequestMapping("/api/gradehistory")
@RestController
public class GradeHistoryController extends ApiController {
    @Autowired
    GradeHistoryRepository gradeHistoryRepository;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    UCSBGradeHistoryService ucsbGradeHistoryService;

    @ApiOperation(value = "Get grade history for a course")
    @GetMapping(value = "/search", produces = "application/json")
    public Iterable<GradeHistory> gradeHistoryBySubjectAreaAndCourseNumber(
        @RequestParam String subjectArea,
        @RequestParam String courseNumber
    )  {
      Iterable<GradeHistory> gradeHistoryRows = gradeHistoryRepository.findBySubjectAreaAndCourse(subjectArea, courseNumber);
      return gradeHistoryRows;
    }

    @ApiOperation(value = "Load grade history into database from uploaded CSV")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "upload", produces = "application/json")
    public ResponseEntity<String> uploadCSV(@RequestPart MultipartFile file) throws IOException{
      log.info("Starting upload CSV");
      try {
        Reader reader = new InputStreamReader(file.getInputStream());
        List<GradeHistory> uploadedRows = ucsbGradeHistoryService.parse(reader);
        List<GradeHistory> savedCourse = (List<GradeHistory>) gradeHistoryRepository.upsertAll(uploadedRows);
        String body = mapper.writeValueAsString(savedCourse);
        return ResponseEntity.ok().body(body);
      } catch(Exception e){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Malformed CSV", e);
      }
    }


}