package com.example.queue.controller;

import com.example.queue.model.Feedback;
import com.example.queue.model.Token;
import com.example.queue.repository.FeedbackRepository;
import com.example.queue.service.AnalyticsService;
import com.example.queue.service.QueueService;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import java.io.ByteArrayOutputStream;
import java.util.*;

@Controller
public class ApiController {

    @Autowired
    private QueueService queueService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private AnalyticsService analyticsService;

    // ---------- PAGE ROUTES THAT REQUIRE BACKEND LOGIC ----------

    // user token registration page
    @GetMapping("/user")
    public String userPage() {
        return "user";
    }

    // staff protected page
    @GetMapping("/staff")
    public String staffPage(HttpSession session) {
        if (session.getAttribute("staffUser") == null) {
            return "redirect:/staff-login";
        }
        return "staff";
    }

    @GetMapping("/staff-login")
    public String staffLoginPage() {
        return "staff-login";
    }

    @PostMapping("/staff-login")
    public String doStaffLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session,
                               HttpServletRequest req) {

        if ("staff".equals(username) && "pass123".equals(password)) {
            session.setAttribute("staffUser", username);
            return "redirect:/staff";
        }
        req.setAttribute("loginError", "Access denied!");
        return "staff-login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session){
        session.invalidate();
        return "redirect:/";
    }

    // ---------- API ROUTES (PURE REST) ----------

    @PostMapping("/api/token")
    @ResponseBody
    public Token createToken(@RequestParam String branch,
                             @RequestParam(defaultValue = "NORMAL") String category) {
        return queueService.createToken(branch, category.toUpperCase());
    }

    @PostMapping("/api/token/register")
    @ResponseBody
    public Token createTokenWithPatient(@RequestParam String branch,
                                        @RequestParam(defaultValue = "NORMAL") String category,
                                        @RequestParam(required = false) String patientName,
                                        @RequestParam(required = false) Integer age,
                                        @RequestParam(required = false) String phone,
                                        @RequestParam(required = false) String symptoms,
                                        @RequestParam(required = false, defaultValue = "false") boolean emergency) {

        String cat = (emergency ? "EMERGENCY" : category).toUpperCase();
        return queueService.createToken(branch, cat, patientName, age, phone, symptoms);
    }

    @GetMapping("/api/waiting")
    @ResponseBody
    public List<Token> waiting(@RequestParam(defaultValue = "Main") String branch) {
        return queueService.getWaiting(branch);
    }

    @PostMapping("/api/next")
    @ResponseBody
    public ResponseEntity<?> next(@RequestParam String branch, @RequestParam String counter) {
        Optional<Token> opt = queueService.getNextForBranch(branch);
        if (opt.isPresent()) {
            Token t = opt.get();
            queueService.markServing(t, counter);
            return ResponseEntity.ok(t);
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/api/complete")
    @ResponseBody
    public ResponseEntity<?> complete(@RequestParam String code) {
        Optional<Token> opt = queueService.findByCode(code);
        if (opt.isPresent()) {
            queueService.markCompleted(opt.get());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/api/feedback")
    @ResponseBody
    public ResponseEntity<?> feedback(@RequestBody Feedback f){
        feedbackRepository.save(f);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/api/analytics")
    @ResponseBody
    public Map<String, Object> analytics(){
        Map<String,Object> m = new HashMap<>();
        m.put("averageRating", analyticsService.averageRating());
        m.put("totalVisitors", analyticsService.totalVisitorsToday());
        return m;
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public Map<String, Integer> stats() {
        List<Token> all = analyticsService.allToday();

        Map<String, Integer> m = new HashMap<>();
        m.put("total", all.size());
        m.put("normal", (int) all.stream().filter(t -> "NORMAL".equals(t.getPriorityCategory())).count());
        m.put("senior", (int) all.stream().filter(t -> "SENIOR".equals(t.getPriorityCategory())).count());
        m.put("vip", (int) all.stream().filter(t -> "VIP".equals(t.getPriorityCategory())).count());
        m.put("emergency", (int) all.stream().filter(t -> "EMERGENCY".equals(t.getPriorityCategory())).count());
        return m;
    }

    @GetMapping("/api/now-serving")
    @ResponseBody
    public Map<String, String> nowServing(@RequestParam(defaultValue = "Main") String branch) {
        Map<String,String> r = new HashMap<>();
        Optional<Token> opt = queueService.getCurrentlyServing(branch);
        if (opt.isPresent()) {
            Token t = opt.get();
            r.put("tokenCode", t.getTokenCode());
            r.put("counter", t.getCounter() == null ? "N/A" : t.getCounter());
            r.put("patientName", t.getPatientName() == null ? "" : t.getPatientName());
            r.put("status", t.getStatus());
        } else {
            r.put("tokenCode", "");
            r.put("counter", "");
            r.put("patientName", "");
            r.put("status", "IDLE");
        }
        return r;
    }

    @GetMapping("/api/qr")
    public ResponseEntity<byte[]> qr(@RequestParam String text,
                                     @RequestParam(defaultValue="200") int size) throws Exception {

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);

        byte[] bytes = baos.toByteArray();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);

        return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
    }
}
