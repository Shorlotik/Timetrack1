package org.example.timetrack.service;

import lombok.RequiredArgsConstructor;
import org.example.timetrack.dto.ProjectDTO;
import org.example.timetrack.entity.Project;
import org.example.timetrack.entity.User;  // Добавлен импорт
import org.example.timetrack.repository.ProjectRepository;
import org.example.timetrack.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    // Создание проекта
    public ProjectDTO createProject(ProjectDTO projectDTO) {
        // Проверяем, существует ли пользователь с таким ID
        User user = userRepository.findById(projectDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setUser(user);

        Project savedProject = projectRepository.save(project);
        return convertToDTO(savedProject);
    }

    // Получение всех проектов
    public List<ProjectDTO> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Получение проекта по ID
    public Optional<ProjectDTO> getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(this::convertToDTO);
    }

    // Обновление проекта
    public Optional<ProjectDTO> updateProject(Long id, ProjectDTO projectDTO) {
        return projectRepository.findById(id).map(existingProject -> {
            existingProject.setName(projectDTO.getName());
            existingProject.setDescription(projectDTO.getDescription());

            // Проверяем, есть ли новый пользователь в обновленном проекте
            if (projectDTO.getUserId() != null && !existingProject.getUser().getId().equals(projectDTO.getUserId())) {
                User newUser = userRepository.findById(projectDTO.getUserId())
                        .orElseThrow(() -> new RuntimeException("User not found"));
                existingProject.setUser(newUser);
            }

            projectRepository.save(existingProject);
            return convertToDTO(existingProject);
        });
    }

    // Удаление проекта
    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Преобразование проекта в DTO
    private ProjectDTO convertToDTO(Project project) {
        return new ProjectDTO(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getUser() != null ? project.getUser().getId() : null // Проверка на null для user
        );
    }

    // Получение проектов пользователя
    public List<ProjectDTO> getProjectsByUser(Long userId) {
        return projectRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // Поиск проектов по названию
    public List<ProjectDTO> searchProjectsByName(String name) {
        return projectRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}
