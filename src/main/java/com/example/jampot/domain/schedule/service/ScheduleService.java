package com.example.jampot.domain.schedule.service;

import com.example.jampot.domain.schedule.domain.Schedule;
import com.example.jampot.domain.schedule.dto.request.CreateScheduleRequest;
import com.example.jampot.domain.schedule.dto.request.EditScheduleRequest;
import com.example.jampot.domain.schedule.dto.response.ScheduleDetailInfoResponse;
import com.example.jampot.domain.schedule.dto.response.ScheduleSimpleInfo;
import com.example.jampot.domain.schedule.repository.ScheduleRepository;
import com.example.jampot.domain.playRoom.domain.PlayRoom;
import com.example.jampot.domain.playRoom.repository.PlayRoomRepository;
import com.example.jampot.domain.user.domain.User;
import com.example.jampot.domain.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final PlayRoomRepository playRoomRepository;
    private final UserRepository userRepository;

    public boolean verifyPassword(Long scheduleId, String inputPassword) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new EntityNotFoundException("해당 일정이 존재하지 않습니다."));

        //평문 비교
        return schedule.getPlayRoom().getAudiencePW().equals(inputPassword);

    }

    @Transactional
    public void createSchedule(Long playRoomId, CreateScheduleRequest request){
        PlayRoom playRoom = playRoomRepository.findById(playRoomId)
                .orElseThrow(() -> new IllegalArgumentException("해당하는 합주실을 찾을 수 없습니다."));

        List<String> requestedNicknames = request.nickNameList();
        List<User> participants = userRepository.findByNickNameIn(requestedNicknames);


        List<String> notFoundNicknames = requestedNicknames.stream()
                        .filter(nickname -> participants.stream().noneMatch(user -> user.getNickName().equals(nickname)))
                        .collect(Collectors.toList());

        if(!notFoundNicknames.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "다음 닉네임을 가진 유저를 찾을 수 없습니다: " + String.join(", ", notFoundNicknames));
        }

        Schedule schedule = Schedule.createSchedule(playRoom, request.date(), request.localTime(), participants);
        scheduleRepository.save(schedule);
    }

    @Transactional
    public void editSchedule(Long scheduleId, EditScheduleRequest request){
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new EntityNotFoundException("해당 일정이 존재하지 않습니다."));

        LocalDate newDate = (request.date()!=null) ? request.date() : schedule.getDate();
        LocalTime newTime = (request.startTime()!=null) ? request.startTime() : schedule.getStartTime();

        Optional<Schedule> anotherSchedule = scheduleRepository.findByPlayRoomIdAndDateAndStartTime(schedule.getPlayRoom().getId(), newDate, newTime);
        if(anotherSchedule.isPresent() && !anotherSchedule.get().equals(schedule)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "합주실 내의 다른 일정과 시간이 겹칩니다.");
        };

        List<User> participants = userRepository.findByNickNameIn(request.nickNameList());
        schedule.updateSchedule(request.date(), request.startTime(), participants);

    }

    @Transactional
    public void deleteSchedule(Long scheduleId){
        scheduleRepository.deleteById(scheduleId);
    }

    @Transactional
    public ScheduleDetailInfoResponse getScheduleDetailInfo(Long scheduleId){
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new EntityNotFoundException("해당 일정이 존재하지 않습니다."));
        List<String> nickNames = schedule.getParticipants().stream()
                .map(participant -> participant.getUser().getNickName())
                .toList();

        return new ScheduleDetailInfoResponse(schedule.getDate(), schedule.getStartTime(), nickNames);
    }

    @Transactional(readOnly = true)
    public List<ScheduleSimpleInfo> getMonthlySchedule(Long playRoomId, Integer month) {
        YearMonth yearMonth = YearMonth.of(LocalDate.now().getYear(), month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<Schedule> schedules = scheduleRepository.findByPlayRoomIdAndDateBetween(playRoomId, startDate, endDate);
        return schedules.stream()
                .map(schedule -> new ScheduleSimpleInfo(
                        schedule.getId(),
                        schedule.getDate()
                ))
                .toList();
    }
}

