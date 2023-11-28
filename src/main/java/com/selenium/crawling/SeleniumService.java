package com.selenium.crawling;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SeleniumService {

    private String mapUrl = "https://map.naver.com/p/search/";

//    @Scheduled(cron = "50 10 15 * * *") // 매일 15시 10분 50초에 실행
    public void selenium() throws InterruptedException {
//        String result = "";
        String cafe = "역삼역 식당";
//        result += "<h1>☕ 단양역 카페 ☕</h1>" + "<br />";
        List<String> links = new ArrayList<>(); // 카페 상세링크들 모음
        String encodedCafe = URLEncoder.encode(cafe, StandardCharsets.UTF_8); // 인코딩

        System.out.println("####START####");
        Path path = Paths.get("C:\\Users\\SSAFY\\Desktop\\chromedriver89\\chromedriver.exe"); // 경로
        System.setProperty("webdriver.chrome.driver", path.toString()); // WebDriver 경로 설정

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-popup-blocking");   // 팝업 안띄움
//        options.addArguments("headless");   // 브라우저 안띄움
        options.addArguments("--disable-gpu");  // gpu 비활성화
//        options.addArguments("--blink-settings=imagesEnabled=false");   // 이미지 다운 안받음

        WebDriver driver = new ChromeDriver(options);

        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(20));    // 드라이버가 실행된 후 20초 기다림
        String searchUrl = mapUrl + encodedCafe + "?c=15.00,0,0,0,dh";

        driver.get(searchUrl);

        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS); // 페이지 전체가 로딩될때까지 기다림

        driver.switchTo().frame(driver.findElement(By.cssSelector("#searchIframe"))); // 검색 목록으로 frame 이동

        int i=1;
//        while(true) {
        if(driver.findElement(By.cssSelector("div.XUrfU")).getText().contains("없습니다")) {
        } else {
            webDriverWait.until(
                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#_pcmap_list_scroll_container > ul > li"))
            ); // 해당 요소 찾을때까지 최대 20초 대기

            // 스크롤 다운 (네이버 지도의 경우 한 페이지에 50개의 장소가 뜸 - 광고까지 54개)
            for(int j=0; j<=5; j++) {
                List<WebElement> scrolls = driver.findElements(By.cssSelector("#_pcmap_list_scroll_container > ul > li")); // 장소 목록 저장
                WebElement lastlist = scrolls.get(scrolls.size() -1); // 장소 목록 중 마지막 장소 요소 저장
                ((JavascriptExecutor)driver).executeScript("arguments[0].scrollIntoView();", lastlist); // 마지막 장소가 위에 올때까지 스크롤
                Thread.sleep(500); // 대기
            }

            List<WebElement> contents = driver.findElements(By.cssSelector("#_pcmap_list_scroll_container > ul > li"));
//        System.out.println(contents.get(contents.size()-1).getText());

            System.out.println(contents.size());

            if(contents.size() > 0) {
                for (WebElement content : contents) {
                    String classAttribute = content.getAttribute("class");
                    if(classAttribute.contains("cZnHG")) continue;

                    WebElement nameElement = content.findElement(By.cssSelector("#_pcmap_list_scroll_container > ul > li > div.CHC5F > a > div > div > span.place_bluelink.TYaxT"));
                    String name = nameElement.getText();
                    nameElement.click(); // 장소 상호명 클릭

                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000)); // 대기

                    driver.switchTo().defaultContent(); // 상위 프레임으로 이동
                    webDriverWait.until(
                            ExpectedConditions.presenceOfElementLocated(By.cssSelector("#entryIframe"))
                    );

                    String link = "";
                    try {
                        link = driver.findElement(By.cssSelector("#entryIframe")).getAttribute("src");
                    } catch (StaleElementReferenceException e) {
                        WebElement entryIframe = driver.findElement(By.cssSelector("#entryIframe"));
                        link = entryIframe.getAttribute("src");
                    }

                    links.add(link);

//                    result += i + ". 카페 : " + name + "<br /> " + "주소 : " + link + "<br />";
                    System.out.println(i + ". 카페 : " + name + " / 주소 : " + link);

                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
                    WebElement button = driver.findElement(By.cssSelector("#section_content > div > div.sc-1wsjitl.OWZjJ > button.sc-lc28fh.bFIegC")); // 상세 정보 iframe 닫기 버튼
                    button.click(); // 닫기 버튼 클릭
                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
                    driver.switchTo().frame(driver.findElement(By.cssSelector("#searchIframe"))); // 검색 목록으로 frame 이동
                    driver.manage().timeouts().implicitlyWait(Duration.ofMillis(5000));
//                    i++;
                }
            }
        }


            // 페이지 이동
//            List<WebElement> pageNums = driver.findElements(By.cssSelector("#app-root > div > div.XUrfU > div.zRM9F > a.mBN2s")); // 페이지 번호 리스트
//            WebElement nextPageNum = null; // 다음 페이지 번호
//            for(WebElement pageNum : pageNums) {
//                String pageClassAttribute = pageNum.getAttribute("class");
//                int idx = 0; // pageNums 리스트에서 pageNum의 인덱스
//                if(pageClassAttribute.contains("qxokY")) { // 현재 페이지 찾았을 때
//                    idx = pageNums.indexOf(pageNum); // 현재 페이지의 리스트 인덱스
//                    if(idx == pageNums.size() - 1) { // 현재 페이지가 마지막 페이지일 경우
//                        break;
//                    } else { // 현재 페이지가 마지막 페이지가 아닐 경우
//                        idx++; // 인덱스 +1
//                        nextPageNum = pageNums.get(idx); // 다음 페이지 번호 지정
//                        nextPageNum.click(); // 클릭
//                        break;
//                    }
//                }
//            }
//            if (nextPageNum == null) break;
//        }

//        result += "<br /><br /><br /><br /><br />";

        for(int cnt = 0; cnt < links.size(); cnt++) {
            WebDriver driverinfo = new ChromeDriver(options);
            WebDriverWait wait = new WebDriverWait(driverinfo, Duration.ofSeconds(10));    // 드라이버가 실행된 후 20초 기다림

            driverinfo.get(links.get(cnt));

            driverinfo.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS); // 페이지 전체가 로딩될때까지 기다림
            wait.until(
                    (ExpectedCondition<Boolean>) webDriver ->
                            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
            );
//            wait.until(
//                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#_title > span.Fc1rA"))
//            );

            /**
             * 상호명, 카테고리
             */
            String name = driverinfo.findElement(By.cssSelector("#_title > span.Fc1rA")).getText(); // 상호명
            String category = driverinfo.findElement(By.cssSelector("#_title > span.DJJvD")).getText(); // 카테고리

//            result += "<div style=\"border:1px solid #9381FF; font-size: 14px;\">" + (cnt+1) + "번째 카페 : " + name + " / 카테고리 : " + category + "<br />";
            System.out.println(cnt+1 + "번째 카페 : " + name + " / 카테고리 : " + category);

            /**
             * 주소
             */
//            wait.until(
//                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#app-root > div > div > div > div > div > div > div > div > div.O8qbU.tQY7D > div > a > span.LDgIH"))
//            );
            wait.until(
                    (ExpectedCondition<Boolean>) webDriver ->
                            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
            );
            String address = driverinfo.findElement(By.cssSelector("#app-root > div > div > div > div > div > div > div > div > div.O8qbU.tQY7D > div > a > span.LDgIH")).getText(); // 주소
//            result += "주소 : " + address + "<br />";
            System.out.println("주소 : " + address);

            /**
             * 별점, 방문자 리뷰 수
             */
            float rating; // 별점
            int reviewCnt; // 방문자 리뷰 수
            WebElement reviewinfo = driverinfo.findElement(By.cssSelector("#app-root > div > div > div > div.place_section.no_margin.OP4V8 > div.zD5Nm.undefined > div.dAsGb"));

            if(reviewinfo.getAttribute("innerHTML").isEmpty()) { // 리뷰 정보가 전혀 없는 경우
                rating = 0.0f;
                reviewCnt = 0;
            } else { // 리뷰 정보가 있는 경우
                WebElement firstElement = driverinfo.findElement(By.cssSelector("#app-root > div > div > div > div.place_section.no_margin.OP4V8 > div.zD5Nm.undefined > div.dAsGb > span:nth-child(1)"));
                String firstElementAttribute = firstElement.getAttribute("class");
                if(firstElementAttribute.contains("LXIwF")) { // 별점이 있는 경우
                    rating = Float.parseFloat(firstElement.getText().replaceAll("[^0-9.]", "")); // 별점 float로 저장
                    WebElement visitCntElement = driverinfo.findElement(By.cssSelector("#app-root > div > div > div > div.place_section.no_margin.OP4V8 > div.zD5Nm.undefined > div.dAsGb > span:nth-child(2)")); // 두 번째 요소
                    String visit = visitCntElement.getText();
                    String[] parts = visit.split(" ");

                    if(visit.contains("방문자리뷰")) {
                        reviewCnt = Integer.parseInt(parts[1].replace(",", ""));
                    } else {
                        reviewCnt = 0;
                    }
                } else { // 별점이 없는 경우
                    rating = 0.0f;
                    String visit = firstElement.getText();
                    String[] parts = visit.split(" ");

                    if(visit.contains("방문자리뷰")) {
                        reviewCnt = Integer.parseInt(parts[1].replace(",", ""));
                    } else {
                        reviewCnt = 0;
                    }
                }
            }
//            result += "⭐ " + rating + "  |  🙍‍♀️ "+ reviewCnt + "<br />";
            System.out.println("⭐ " + rating + "  |  🙍‍♀️ "+ reviewCnt);

            /**
             * 이미지 링크
             */
            String image = null; // 이미지 링크
//            wait.until(
//                    ExpectedConditions.presenceOfElementLocated(By.cssSelector("#app-root > div > div > div > div.CB8aP > div"))
//            );
            wait.until(
                    (ExpectedCondition<Boolean>) webDriver ->
                            ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
            );
            WebElement imageElement = driverinfo.findElement(By.cssSelector("#app-root > div > div > div > div.CB8aP > div"));
            String imageClassAttribute = imageElement.getAttribute("class");
            if(imageClassAttribute.contains("uDR4i")) { // 업체 사진 등록된 경우
                WebElement imageLinkElement = driverinfo.findElement(By.cssSelector("#_autoPlayable > div"));
                String imageStyle = imageLinkElement.getAttribute("style");
                String regex = "url\\((.*?)\\)";

                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(imageStyle);

                if(matcher.find()) {
                    image = matcher.group(1);
                    image = image.replace("\"", "");
//                    result += "<img src=\"" + image + "\" alt=\"image\" width=\"200\"><br />";
                }
            } else { // 업체 사진 등록 안 된 경우
                image = "등록된 사진 없음";
//                result += "🖼 : " + image + "<br />";
            }
            System.out.println("🖼 : " + image);

            /**
             * 운영시간, 상세 정보
             */
            String detail = "정보 없음"; // 상세 정보
            Map<Integer, Long> times = new HashMap<>(); // 운영시간
            String defaultTime = convertTimeToBits("12:00 - 18:00"); // 기본시간

            List<WebElement> infos = driverinfo.findElements(By.cssSelector("#app-root > div > div > div > div:nth-child(5) > div > div.place_section.no_margin > div > div > div.O8qbU")); // 정보 목록

            for(int num = 0; num < infos.size(); num++) {
                WebElement infoElement = infos.get(num); // 현재 정보 요소
                String infoClassAttribute = infoElement.getAttribute("class"); // 클래스이름
                if(infoClassAttribute.contains("jKv4e")) continue; // 세스코멤버스 기호
                else if(infoClassAttribute.contains("NqeGY")) continue; // 플레이스 관리 권유 버튼

                try {
                    WebElement titleElement = infoElement.findElement(By.cssSelector("strong > span.place_blind"));
                    if(titleElement.getText().equals("설명")) { // 장소에 대한 설명인 경우
                        WebElement descriptionElement = infoElement.findElement(By.cssSelector("span.zPfVt"));
                        if(descriptionElement.getAttribute("innerText").trim().isEmpty()) {
                            detail = "정보 없음";
                        } else {
                            descriptionElement.click();
                            Thread.sleep(300);
                            detail = descriptionElement.getText();
                        }
                    } else if(titleElement.getText().contains("영업시간")) { // 운영시간에 대한 설명인 경우
                        if(infoClassAttribute.contains("J1zN9")) { // 운영시간이 등록되지 않은 경우 - 월 ~ 일 모두 동일한 시간 임의로 부여
                            for (int day = 1; day <= 7; day++) {
                                times.put(day, Long.parseLong(defaultTime, 2));
                            }
                            break;
                        } else if(infoClassAttribute.contains("pSavy")) { // 운영시간이 등록된 경우
                            WebElement timeinfoElement = infoElement.findElement(By.cssSelector("div.y6tNq"));
                            timeinfoElement.click(); // 클릭
                            Thread.sleep(300); // 대기

                            List<WebElement> timeElement = infoElement.findElements(By.cssSelector("div.vV_z_ > a > div.w9QyJ")); // 요일별 운영시간 정보

                            if(timeElement.get(1).findElement(By.cssSelector("div.y6tNq > span.A_cdD > span.i8cJw")).getText().contains("매일")) { // 운영시간이 "매일"로 등록된 경우
                                for(WebElement time : timeElement) {
                                    if (time.getAttribute("class").contains("vI8SM") || time.getAttribute("class").contains("DzD3b")) continue; // 오늘 운영시간 정보인 경우
                                    else if (time.getAttribute("class").contains("yN6TD")) continue; // 매장 휴무 정보인 경우
                                    else {
                                        for (int day = 1; day <= 7; day++) {
                                            times.put(day, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        }
                                        break;
                                    }
                                }
                            } else {
                                for(WebElement time : timeElement) {
                                    if (time.getAttribute("class").contains("vI8SM") || time.getAttribute("class").contains("DzD3b")) continue; // 오늘 운영시간 정보인 경우
                                    else if (time.getAttribute("class").contains("yN6TD")) continue; // 매장 휴무 정보인 경우
                                    else {
                                        String day = time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > span.i8cJw")).getText();

                                        if(day.charAt(0) == '월') {
                                            times.put(1, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        } else if(day.charAt(0) == '화') {
                                            times.put(2, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        } else if(day.charAt(0) == '수') {
                                            times.put(3, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        } else if(day.charAt(0) == '목') {
                                            times.put(4, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        } else if(day.charAt(0) == '금') {
                                            times.put(5, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        } else if(day.charAt(0) == '토') {
                                            times.put(6, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        } else if(day.charAt(0) == '일') {
                                            times.put(7, Long.parseLong(convertTimeToBits(time.findElement(By.cssSelector("div.y6tNq > span.A_cdD > div.H3ua4")).getText()), 2));
                                        }
                                    }
                                }
                            }
                        }

                    }
                } catch (NoSuchElementException e) {
                    continue;
                }
            }
            if(times.size() == 0) {
                for (int day = 1; day <= 7; day++) {
                    times.put(day, Long.parseLong(defaultTime, 2));
                }
            }

//            result += "ℹ : " + detail + "<br />";
            System.out.println("ℹ : " + detail);

//            result += "<h4> 운영시간 </h4>";
            System.out.println("운영시간");
            for(Map.Entry<Integer, Long> time : times.entrySet()) {
//                result += time.getKey() + " : " + time.getValue() + "<br />";
                System.out.println(time.getKey() + " : " + time.getValue());
            }
//            result += "</div><br />";

            driverinfo.quit();
            Thread.sleep(500);
        }

        driver.quit();

        System.out.println("####END####");

//        return result;

    }

    public String convertTimeToBits(String timeRange) {
        StringBuilder result = new StringBuilder();

        timeRange = timeRange.replace(" - ", " ");
        timeRange = timeRange.replace("\n", " ");
        String[] times = timeRange.split(" ");
        int[] bitArray = new int[48];

        try {
            String startTime = times[0];
            String endTime = times[1];

            int startMinutes = convertToMinutes(startTime);
            int endMinutes = convertToMinutes(endTime);

            if(startMinutes == -1 || endMinutes == -1) { // 쉬는 날인 경우
            } else if(startMinutes < endMinutes) { // 시작시간보다 끝나는 시간이 늦을 경우 (ex. 10:00 - 19:00)
                for(int mm = startMinutes; mm < endMinutes; mm += 30) {
                    bitArray[mm / 30] = 1;
                }
            } else if(startMinutes > endMinutes) { // 시작시간보다 끝나는 시간이 빠를 경우 (ex. 10:00 - 02:00)
                for(int mm = startMinutes; mm < 24 * 60; mm += 30) {
                    bitArray[mm / 30] = 1;
                }
                for(int mm = 0; mm < endMinutes; mm += 30) {
                    bitArray[mm / 30] = 1;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }


        for(int bit : bitArray) {
            result.append(bit);
        }

        return result.toString();
    }

    public int convertToMinutes(String time) {
        String[] parts = time.split(":");
        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            return hours * 60 + minutes;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
