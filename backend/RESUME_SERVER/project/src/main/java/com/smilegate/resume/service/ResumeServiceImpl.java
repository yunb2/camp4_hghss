package com.smilegate.resume.service;

import com.smilegate.resume.domain.Answer;
import com.smilegate.resume.domain.Company;
import com.smilegate.resume.domain.Resume;
import com.smilegate.resume.dto.request.ResumeRequestDto;
import com.smilegate.resume.dto.response.ResumeDetailResponseDto;
import com.smilegate.resume.exceptions.AnswerNotExistException;
import com.smilegate.resume.exceptions.ResumeNotExistException;
import com.smilegate.resume.exceptions.UnauthorizedException;
import com.smilegate.resume.repository.ResumeRepository;
import com.smilegate.resume.utils.DateUtil;
import com.smilegate.resume.utils.JwtUtil;
import com.smilegate.resume.utils.RedisUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ResumeServiceImpl implements ResumeService {

    private final ResumeRepository resumeRepository;
    private final JwtUtil jwtUtil;
    private final DateUtil dateUtil;
    private final RedisUtil redisUtil;

    @Override
    @Transactional
    public ResumeDetailResponseDto createResume(String token, int positionId, ResumeRequestDto resumeRequestDto) {

        int userId = getUserId(token);
        int recruitId = resumeRepository.findRecruitIdByPositionId(positionId);
        Company company = resumeRepository.findCompanyByRecruitId(recruitId);

        Resume resume = Resume.builder()
                            .userId(userId)
                            .recruitId(recruitId)
                            .companyId(company.getId())
                            .positionId(positionId)
                            .title(resumeRequestDto.getTitle())
                            .endTime(resumeRequestDto.getEndTime())
                            .build();

        int resumeId = resumeRepository.createResume(resume);
        resume = resumeRepository.findResumeById(resumeId);

        List<Answer> answers = resumeRequestDto.getAnswers();
        for(int i=0; i<answers.size(); ++i) {
            Answer answer = answers.get(i);
            answer.setResumeId(resumeId);
            answer.setOrderNum(i+1);

            resumeRepository.createAnswer(answer);
        }

        resumeRepository.updateResumeCount(recruitId);

        String endtime = dateUtil.changeFormat(resume.getEndTime());
        StringBuilder key = new StringBuilder();
        key.append(endtime).append(":");
        key.append(recruitId).append(":");
        key.append(company.getId()).append(":");
        key.append(company.getName());

        if(redisUtil.reverseRank("ranking-apply", key.toString()) != null) {
            redisUtil.increaseScore("ranking-apply", key.toString(), 1);
        } else {
            redisUtil.add("ranking-apply", key.toString(), 1);
        }

        return ResumeDetailResponseDto.builder()
                .resume(resume)
                .answers(answers)
                .build();
    }

    @Override
    public List<Resume> getResumes(String token) {
        int userId = getUserId(token);
        List<Resume> resumes = resumeRepository.findResumesByUserId(userId);
        return resumes;
    }

    @Override
    @Transactional
    public boolean saveResume(int resumeId, String token, String title, List<Answer> answers) {

        if(!checkAuth(token, resumeId)) return false;

        resumeRepository.updateTitle(resumeId, title, dateUtil.now());

        for(int i=0; i<answers.size(); ++i) {
            Answer answer = answers.get(i);
            answer.setResumeId(resumeId);
            int updateCnt = resumeRepository.updateAnswer(answer);
            if(updateCnt < 1) throw new AnswerNotExistException(answer.getId());
        }

        return true;
    }

    @Override
    @Transactional
    public boolean deleteResume(int resumeId, String token) {

        if(!checkAuth(token, resumeId)) return false;

        resumeRepository.deleteAnswers(resumeId);
        resumeRepository.deleteResume(resumeId);

        return true;
    }

    @Override
    public boolean moveResume(int resumeId, String token, int col, int row) {

        if(!checkAuth(token, resumeId)) return false;

        resumeRepository.updateResumePosition(resumeId, col, row);

        return true;
    }

    @Override
    public ResumeDetailResponseDto getResume(String token, int resumeId) {

        if(!checkAuth(token, resumeId)) return null;

        Resume resume = resumeRepository.findResumeById(resumeId);
        List<Answer> answers = resumeRepository.findAnswersByResumeId(resumeId);

        return ResumeDetailResponseDto.builder()
                .resume(resume)
                .answers(answers)
                .build();
    }

    @Override
    public Answer createAnswer(String token, int resumeId) {

        if(!checkAuth(token, resumeId)) return null;

        int order = resumeRepository.findMaxOrderNumByResumeId(resumeId);

        Answer answer = Answer.builder()
                            .resumeId(resumeId)
                            .orderNum(order+1)
                            .questionContent("")
                            .answerContent("")
                            .questionLimit(1000)
                            .build();

        resumeRepository.createAnswer(answer);

        return answer;
    }

    @Override
    public boolean deleteAnswer(String token, int answerId) {

        Integer resumeId = resumeRepository.findResumeIdByAnswerId(answerId);
        if(resumeId == null) throw new AnswerNotExistException(answerId);

        if(!checkAuth(token, resumeId)) return false;

        resumeRepository.deleteAnswer(answerId);

        return true;
    }

    @Override
    public int countResume(String token, int positionId) {
        int userId = getUserId(token);
        return resumeRepository.countResumeByPositionId(userId, positionId);
    }

    private boolean checkAuth(String token, int resumeId) {

        Resume resume = resumeRepository.findResumeById(resumeId);
        if(resume == null) throw new ResumeNotExistException(resumeId);

        int userId = getUserId(token);
        if(userId != resume.getUserId()) throw new UnauthorizedException();

        return true;
    }

    private int getUserId(String token) {
        Claims claims = jwtUtil.getClaims(token.substring("Bearer ".length()));
        return (int) claims.get("userId");
    }
}
