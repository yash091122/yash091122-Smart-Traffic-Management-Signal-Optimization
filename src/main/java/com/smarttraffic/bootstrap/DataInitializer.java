package com.smarttraffic.bootstrap;

import com.smarttraffic.entity.Intersection;
import com.smarttraffic.entity.Role;
import com.smarttraffic.entity.RoadSegment;
import com.smarttraffic.entity.SignalPhase;
import com.smarttraffic.repository.IntersectionRepository;
import com.smarttraffic.repository.RoleRepository;
import com.smarttraffic.repository.RoadSegmentRepository;
import com.smarttraffic.repository.UserRepository;
import com.smarttraffic.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final IntersectionRepository intersectionRepository;
    private final RoadSegmentRepository roadSegmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    @Order(1)
    CommandLineRunner seedUsersAndGraph() {
        return args -> {
            if (roleRepository.count() == 0) {
                roleRepository.save(new Role(Role.RoleName.ADMIN));
                roleRepository.save(new Role(Role.RoleName.VIEWER));
            }
            Role adminRole = roleRepository.findByName(Role.RoleName.ADMIN).orElseThrow();
            Role viewerRole = roleRepository.findByName(Role.RoleName.VIEWER).orElseThrow();

            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setRoles(Set.of(adminRole));
                userRepository.save(admin);
                log.info("Created user admin / adminpass");
            }
            if (userRepository.findByUsername("viewer").isEmpty()) {
                User viewer = new User();
                viewer.setUsername("viewer");
                viewer.setPassword(passwordEncoder.encode("viewerpass"));
                viewer.setRoles(Set.of(viewerRole));
                userRepository.save(viewer);
                log.info("Created user viewer / viewerpass");
            }

            if (intersectionRepository.count() == 0) {
                // Central Delhi
                Intersection a = intersection("ITO Crossing", 28.6280, 77.2410, 4, true);
                Intersection b = intersection("Connaught Place", 28.6315, 77.2167, 4, true);
                Intersection c = intersection("India Gate Circle", 28.6129, 77.2295, 3, true);
                Intersection d = intersection("Chandni Chowk", 28.6506, 77.2334, 3, true);

                // South Delhi
                Intersection e = intersection("AIIMS Crossing", 28.5672, 77.2100, 3, false);
                Intersection f = intersection("Hauz Khas Junction", 28.5494, 77.2001, 2, false);
                Intersection g = intersection("Nehru Place Flyover", 28.5491, 77.2530, 2, false);

                // West & North Delhi
                Intersection h = intersection("Rajouri Garden Metro", 28.6493, 77.1215, 3, true);
                Intersection i = intersection("Punjabi Bagh Chowk", 28.6682, 77.1310, 2, false);
                Intersection j = intersection("Rohini Sec-3 Signal", 28.7159, 77.1166, 3, true);

                // East Delhi & Noida
                Intersection k = intersection("Akshardham Crossing", 28.6127, 77.2773, 2, false);
                Intersection l = intersection("Noida Sec-18 Signal", 28.5700, 77.3210, 2, false);

                // Ring Road Corridors
                Intersection m = intersection("Moolchand Flyover", 28.5688, 77.2375, 3, true);
                Intersection n = intersection("Dhaula Kuan Junction", 28.5930, 77.1660, 2, false);

                // Outer Ring
                Intersection o = intersection("Kashmere Gate ISBT", 28.6674, 77.2295, 2, false);
                Intersection p = intersection("Sarai Kale Khan", 28.5893, 77.2571, 3, true);

                a = intersectionRepository.save(a);
                b = intersectionRepository.save(b);
                c = intersectionRepository.save(c);
                d = intersectionRepository.save(d);
                e = intersectionRepository.save(e);
                f = intersectionRepository.save(f);
                g = intersectionRepository.save(g);
                h = intersectionRepository.save(h);
                i = intersectionRepository.save(i);
                j = intersectionRepository.save(j);
                k = intersectionRepository.save(k);
                l = intersectionRepository.save(l);
                m = intersectionRepository.save(m);
                n = intersectionRepository.save(n);
                o = intersectionRepository.save(o);
                p = intersectionRepository.save(p);

                // Main arterial connections
                roadSegmentRepository.save(seg(a, b, 20)); roadSegmentRepository.save(seg(b, a, 20));
                roadSegmentRepository.save(seg(b, d, 18)); roadSegmentRepository.save(seg(d, b, 18));
                roadSegmentRepository.save(seg(b, e, 10)); roadSegmentRepository.save(seg(e, b, 10));
                roadSegmentRepository.save(seg(a, c, 25)); roadSegmentRepository.save(seg(c, a, 25));
                roadSegmentRepository.save(seg(a, f, 15)); roadSegmentRepository.save(seg(f, a, 15));
                roadSegmentRepository.save(seg(e, g, 12)); roadSegmentRepository.save(seg(g, e, 12));
                roadSegmentRepository.save(seg(e, f, 14)); roadSegmentRepository.save(seg(f, e, 14));
                roadSegmentRepository.save(seg(d, m, 10)); roadSegmentRepository.save(seg(m, d, 10));
                roadSegmentRepository.save(seg(m, n, 22)); roadSegmentRepository.save(seg(n, m, 22));
                roadSegmentRepository.save(seg(a, h, 20)); roadSegmentRepository.save(seg(h, a, 20));
                roadSegmentRepository.save(seg(h, i, 12)); roadSegmentRepository.save(seg(i, h, 12));
                roadSegmentRepository.save(seg(i, j, 18)); roadSegmentRepository.save(seg(j, i, 18));
                roadSegmentRepository.save(seg(h, p, 10)); roadSegmentRepository.save(seg(p, h, 10));
                roadSegmentRepository.save(seg(p, o, 8));  roadSegmentRepository.save(seg(o, p, 8));
                roadSegmentRepository.save(seg(o, k, 14)); roadSegmentRepository.save(seg(k, o, 14));
                roadSegmentRepository.save(seg(k, l, 16)); roadSegmentRepository.save(seg(l, k, 16));
                roadSegmentRepository.save(seg(c, k, 12)); roadSegmentRepository.save(seg(k, c, 12));
                roadSegmentRepository.save(seg(f, k, 18)); roadSegmentRepository.save(seg(k, f, 18));
                roadSegmentRepository.save(seg(n, h, 15)); roadSegmentRepository.save(seg(h, n, 15));
                roadSegmentRepository.save(seg(c, l, 15)); roadSegmentRepository.save(seg(l, c, 15));

                log.info("Seeded 16 intersections and road segments across Delhi NCR");
            }
        };
    }

    private static Intersection intersection(String name, double lat, double lng, int lanes, boolean main) {
        Intersection i = new Intersection();
        i.setName(name);
        i.setLatitude(lat);
        i.setLongitude(lng);
        i.setLaneCount(lanes);
        i.setMainRoad(main);
        i.setCurrentPhase(SignalPhase.RED);
        i.setGreenDuration(25);
        i.setRedDuration(30);
        i.setVehicleCount(5);
        return i;
    }

    private static RoadSegment seg(Intersection from, Intersection to, int seconds) {
        RoadSegment s = new RoadSegment();
        s.setFromIntersection(from);
        s.setToIntersection(to);
        s.setBaseTravelSeconds(seconds);
        return s;
    }
}
