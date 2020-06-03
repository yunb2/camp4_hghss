package com.smilegate.resume.service;

import com.smilegate.resume.domain.Answer;
import com.smilegate.resume.domain.Resume;
import com.smilegate.resume.dto.request.ResumeRequestDto;
import com.smilegate.resume.dto.response.ResumeDetailResponseDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ResumeService {

    ResumeDetailResponseDto createResume(String token, int positionId, ResumeRequestDto resumeRequestDto);

    List<Resume> getResumes(String token);

    boolean saveResume(int resumeId, String token, String title, List<Answer> answers);

    boolean deleteResume(int resumeId, String token);

    boolean moveResume(int resumeId, String token, int col, int row);

    ResumeDetailResponseDto getResume(String token, int resumeId);

    Answer createAnswer(String token, int resumeId);

    boolean deleteAnswer(String token, int answerId);

    int countResume(String token, int positionId);
}
