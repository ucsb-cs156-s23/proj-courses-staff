package edu.ucsb.cs156.courses.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import edu.ucsb.cs156.courses.repositories.UserRepository;
import edu.ucsb.cs156.courses.services.UCSBCurriculumService;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/sections")
public class UCSBSectionsController {
    private final Logger logger = LoggerFactory.getLogger(UCSBSectionsController.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    UserRepository userRepository;

    @Autowired
    UCSBCurriculumService ucsbCurriculumService;

    @GetMapping(value = "/basicsearch", produces = "application/json")
    public ResponseEntity<String> basicsearch(
            @ApiParam(name = "qtr", type = "String", value = "quarter in yyyyq format (e.g. 20221 for W22)", example = "20221", required = true) @RequestParam String qtr,
            @ApiParam(name = "dept", type = "String", value = "subject area, e.g. CMPSC for Computer Science", example = "CMPSC", required = true) @RequestParam String dept,
            @ApiParam(name = "level", type = "String", value = "level, 1 character code: 'G'raduate or 'U'ndergraduate, 'L' for ugrad lower div, 'S' for ugrad upper div, A for All", example = "U", required = true) @RequestParam String level)
            throws JsonProcessingException {
        String body = ucsbCurriculumService.getSectionJSON(dept, qtr, level);
        return ResponseEntity.ok().body(body);
    }

    @GetMapping(value = "/sectionsearch", produces = "application/json")
    public ResponseEntity<String> sectionsearch(@RequestParam String qtr, @RequestParam String enrollCode) {

        String body = ucsbCurriculumService.getSection(enrollCode, qtr);

        return ResponseEntity.ok().body(body);
    }
}
