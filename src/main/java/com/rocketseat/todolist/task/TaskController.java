package com.rocketseat.todolist.task;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rocketseat.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var currentDate = LocalDateTime.now();
        if(currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de início/fim deve ser maior que a data atual");
        }
        
        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de iníciodeve ser maior que a data de término");
        }
        
        taskModel.setIdUser((UUID)request.getAttribute("idUser"));
        this.taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskModel);
    }

    @GetMapping("/")
    public ResponseEntity list(HttpServletRequest request) {
        var tasks = this.taskRepository.findByIdUser((UUID)request.getAttribute("idUser"));
        return ResponseEntity.status(200).body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        var task = this.taskRepository.findById(id).orElse(null);
        var idUser = request.getAttribute("idUser");

        if(task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Task não encontrada.");
        }

        if(!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não tem permissão para alterar essa tarefa.");
        }

        Utils.copyNonNullProperties(taskModel, task);
        this.taskRepository.save(task);
        return ResponseEntity.status(200).body(task);
    }
}
