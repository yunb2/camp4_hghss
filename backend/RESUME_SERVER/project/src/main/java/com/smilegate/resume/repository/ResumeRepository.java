package com.smilegate.resume.repository;

import com.smilegate.resume.domain.Answer;
import com.smilegate.resume.domain.Company;
import com.smilegate.resume.domain.Position;
import com.smilegate.resume.domain.Resume;

import java.util.List;
import java.util.concurrent.Future;

public interface ResumeRepository {

    int createResume(Resume resume);

    Future<Integer> createAnswer(Answer answer);

    int updateAnswer(Answer answer);

    int updateTitle(int id, String title, String date);

    List<Resume> findResumesByUserId(int userId);

    Resume findResumeById(int resumeId);

    int deleteAnswers(int resumeId);

    int deleteResume(int resumeId);

    int updateResumePosition(int resumeId, int col, int row);

    List<Answer> findAnswersByResumeId(int resumeId);

    Integer findResumeIdByAnswerId(int answerId);

    int deleteAnswer(int answerId);

    Integer findRecruitIdByPositionId(int positionId);

    Integer findMaxOrderNumByResumeId(int resumeId);

    int countResumeByPositionId(int userId, int positionId);

    Company findCompanyByRecruitId(int id);

    int updateResumeCount(int recruitId);
}
