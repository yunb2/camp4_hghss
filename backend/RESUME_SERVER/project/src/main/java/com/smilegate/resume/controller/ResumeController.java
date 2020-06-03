package com.smilegate.resume.controller;

import com.smilegate.resume.domain.Answer;
import com.smilegate.resume.domain.Resume;
import com.smilegate.resume.dto.ResultResponse;
import com.smilegate.resume.dto.request.ResumeRequestDto;
import com.smilegate.resume.dto.response.ResumeCountResponseDto;
import com.smilegate.resume.dto.response.ResumeDetailResponseDto;
import com.smilegate.resume.service.ResumeService;
import com.smilegate.resume.service.ResumeServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@RestController
@RequestMapping("/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping("/create/{positionId}")
    public ResponseEntity<ResultResponse> create(
            @RequestHeader("Authorization") String token,
            @PathVariable("positionId") int positionId,
            @RequestBody ResumeRequestDto resumeRequestDto
    ) {
        log.info("RESUME CREATE positionId : "+positionId);
        ResumeDetailResponseDto resumeDetailResponseDto = resumeService.createResume(token, positionId, resumeRequestDto);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("자기소개서를 생성했습니다.")
                        .data(resumeDetailResponseDto)
                        .build()
        );
    }

    @GetMapping("/list")
    public ResponseEntity<ResultResponse> list(@RequestHeader("Authorization") String token) {
        log.info("RESUME LIST");
        List<Resume> resumes = resumeService.getResumes(token);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("자기소개서 목록입니다.")
                        .data(resumes)
                        .build()
        );
    }

    @PutMapping("/save/{resumeId}")
    public ResponseEntity<ResultResponse> save(
            @RequestHeader("Authorization") String token,
            @PathVariable("resumeId") int resumeId,
            @RequestBody ResumeRequestDto resumeRequestDto
    ) {
        log.info("RESUME SAVE resumeId : " + resumeId);
        resumeService.saveResume(resumeId, token, resumeRequestDto.getTitle(), resumeRequestDto.getAnswers());

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("자기소개서를 저장했습니다.")
                        .build()
        );
    }

    @DeleteMapping("/delete/{resumeId}")
    public ResponseEntity<ResultResponse> deleteResume(
            @RequestHeader("Authorization") String token,
            @PathVariable("resumeId") int resumeId
    ) {
        log.info("RESUME DELETE resumeId : " + resumeId);
        resumeService.deleteResume(resumeId, token);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("자기소개서를 삭제했습니다.")
                        .build()
        );
    }

    @PutMapping("/{resumeId}")
    public ResponseEntity<ResultResponse> moveResume(
            @RequestHeader("Authorization") String token,
            @PathVariable int resumeId,
            @RequestParam("col") int col,
            @RequestParam("row") int row
    ) {
        log.info("MOVE REUSME resumeId : " + resumeId);
        resumeService.moveResume(resumeId, token, col, row);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("자기소개서를 이동했습니다.")
                        .build()
        );
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<ResultResponse> detail(
            @RequestHeader("Authorization") String token,
            @PathVariable("resumeId") int resumeId
    ) {
        log.info("RESUME DETAIL resumeId : " + resumeId);
        ResumeDetailResponseDto resumeDetailResponseDto = resumeService.getResume(token, resumeId);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("자기소개서입니다. : resume_id = " + resumeId)
                        .data(resumeDetailResponseDto)
                        .build()
        );
    }

    @PostMapping("/answer/add/{resumeId}")
    public ResponseEntity<ResultResponse> createAnswer(
            @RequestHeader("Authorization") String token,
            @PathVariable int resumeId
    ) {
        log.info("ANSWER CREATE resumeId : " + resumeId);
        Answer answer = resumeService.createAnswer(token, resumeId);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("문항을 추가했습니다.")
                        .data(answer)
                        .build()
        );
    }

    @DeleteMapping("/answer/delete/{answerId}")
    public ResponseEntity<ResultResponse> deleteAnswer(
            @RequestHeader("Authorization") String token,
            @PathVariable int answerId
    ) {
        log.info("ANSWER DELETE answerId : " + answerId);
        resumeService.deleteAnswer(token, answerId);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("문항을 삭제했습니다.")
                        .build()
        );
    }

    @GetMapping("/count/{positionId}")
    public ResponseEntity<ResultResponse> countResume(
            @RequestHeader("Authorization") String token,
            @PathVariable int positionId
    ) {
        log.info("COUNT RESUME positionId : " + positionId);
        int resumeCnt = resumeService.countResume(token, positionId);

        return ResponseEntity.ok().body(
                ResultResponse.builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("해당 직무에 작성한 자기소개서 개수입니다.")
                        .data(ResumeCountResponseDto.builder().resumeCnt(resumeCnt).build())
                        .build()
        );
    }

}
