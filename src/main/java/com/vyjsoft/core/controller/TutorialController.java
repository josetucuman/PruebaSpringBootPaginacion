package com.vyjsoft.core.controller;


import com.vyjsoft.core.model.Tutorial;
import com.vyjsoft.core.respository.TutorialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author jgomez
 */

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class TutorialController {

    @Autowired
    TutorialRepository repository;

    private Sort.Direction getSortDirection(String direction){
        String direccion = direction.toLowerCase();
        if(direccion.equals("asc")){
            return Sort.Direction.ASC;
        } else if(direccion.equals("desc")){
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    /**
     *
     * @param sort
     * @return
     */
    @GetMapping("/sortedtutorials")
    public ResponseEntity<List<Tutorial>> getAllTutorials(@RequestParam(defaultValue = "id, desc")
                                                                      String[] sort){
        try {
            List<Sort.Order> orders = new ArrayList<Sort.Order>();
            if(sort[0].contains(",")){
                // will sort more than 2 fields
                // sortOrder="field, direction"
                for(String sortOrder : sort){
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Order(getSortDirection(_sort[1]), _sort[0]));
                }
            } else {
                orders.add(new Order(getSortDirection(sort[1]), sort[0]));
            }

            List<Tutorial> tutorials = repository.findAll();

            if(tutorials.isEmpty()){
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(tutorials, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/tutorials")
    public ResponseEntity<Map<String, Object>> getAllTutorialsPage(
            @RequestParam(required = false) String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size,
            @RequestParam(defaultValue = "id, desc")String[]sort
    ){
        try {
            List<Order> orders = new ArrayList<Order>();

            if(sort[0].contains(",")){
                for(String sortOrder : sort){
                    String[] _sort = sortOrder.split(",");
                    orders.add(new Order(getSortDirection(_sort[1]),_sort[0]));
                }
            } else {
                orders.add(new Order(getSortDirection(sort[1]),sort[0]));
            }

            List<Tutorial> tutorials = new ArrayList<Tutorial>();
            Pageable pagingSort = PageRequest.of(page,size, Sort.by(orders));

            Page<Tutorial> pageTuts;

            if(title == null)
                pageTuts = repository.findAll(pagingSort);
            else
                pageTuts = repository.findByTitleContaining(title, pagingSort);

            tutorials = pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();

            response.put("tutorials",tutorials);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);



        } catch (Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *
     * @param page
     * @param size
     * @return
     */

    @GetMapping("/tutorials/published")
    public ResponseEntity<Map<String, Object>> findByPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "3") int size){
        try {
            List<Tutorial> tutorials = new ArrayList<Tutorial>();
            Pageable paging = PageRequest.of(page, size);

            Page<Tutorial> pageTuts = repository.findByPublished(true, paging);
            tutorials= pageTuts.getContent();

            Map<String, Object> response = new HashMap<>();
            response.put("tutorials", tutorials);
            response.put("currentPage", pageTuts.getNumber());
            response.put("totalItems", pageTuts.getTotalElements());
            response.put("totalPages", pageTuts.getTotalPages());

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *
     * @param idTutorials
     * @return
     */

    @GetMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> getTutorialByID(@PathVariable("id") Long idTutorials){

        Optional<Tutorial> tutorialData = repository.findById(idTutorials);

        if(tutorialData.isPresent()){
            return new ResponseEntity<>(tutorialData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     *
     * @param tutorial
     * @return
     */
    @PostMapping("/tutorials")
    public ResponseEntity<Tutorial> createTutorial(@RequestBody Tutorial tutorial){
        try {
            Tutorial __tutorial = repository.save(new Tutorial(tutorial.getTitle(),
                    tutorial.getDescription(), false));
            return new ResponseEntity<>(__tutorial, HttpStatus.OK);
        } catch (Exception ex){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *
     * @param id
     * @param tutorial
     * @return
     */

    @PutMapping("/tutorials/{id}")
    public ResponseEntity<Tutorial> actualizarTutorial(@PathVariable("id") Long id,
                                                       @RequestBody Tutorial tutorial){

        Optional<Tutorial> tutorialDATA = repository.findById(id);

        if(tutorialDATA.isPresent()){
            Tutorial __tutorial = tutorialDATA.get();
            __tutorial.setTitle(tutorial.getTitle());
            __tutorial.setDescription(tutorial.getDescription());
            __tutorial.setPublished(tutorial.isPublished());
            return new ResponseEntity<>(repository.save(__tutorial), HttpStatus.OK);
        } else {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }


    /**
     *
     * @param idTutorial
     * @return
     */

    @DeleteMapping("/tutorials/{id}")
    public ResponseEntity<HttpStatus> borrarTutorial(@PathVariable("id") Long idTutorial){

        try {
            repository.deleteById(idTutorial);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception ex){
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     *
     * @return
     */
    @DeleteMapping("/tutorials")
    public ResponseEntity<HttpStatus> deleteAllTutorials() {
        try {
            repository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
}
