package ru.netology.delivery.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import ru.netology.delivery.data.DataGenerator;

import java.time.Duration;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selectors.withText;
import static com.codeborne.selenide.Selenide.*;

class DeliveryTest {

    @BeforeEach
    void setUp() {
        open("http://localhost:9999");
    }

    @Test
    @DisplayName("Should successful plan and replan meeting")
    void shouldSuccessfulPlanAndReplanMeeting() {
        var locale = "ru";
        var validUser = DataGenerator.Registration.generateUser(locale);
        var daysToAddForFirstMeeting = 4;
        var firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        var daysToAddForSecondMeeting = 7;
        var secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);
        $("[data-test-id=city] input").val(validUser.getCity());
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").val(firstMeetingDate);
        $("[data-test-id=name] input").val(validUser.getName());
        $("[data-test-id=phone] input").val(validUser.getPhone());
        $("[data-test-id=agreement]").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=success-notification]").shouldBe(visible, Duration.ofSeconds(15));
        $(".notification__content").shouldHave(text("Встреча успешно запланирована на " + firstMeetingDate))
                .shouldBe(visible, Duration.ofSeconds(15));
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").val(secondMeetingDate);
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=replan-notification]").shouldBe(visible, Duration.ofSeconds(15));
        $$(".notification__content")
                .findBy(text("У вас уже запланирована встреча на другую дату. Перепланировать?"))
                .shouldBe(visible, Duration.ofSeconds(15));
        $$("button").find(exactText("Перепланировать")).click();
        $("[data-test-id=success-notification]").shouldBe(visible, Duration.ofSeconds(15));
        $(".notification__content").shouldHave(text("Встреча успешно запланирована на " + secondMeetingDate))
                .shouldBe(visible, Duration.ofSeconds(15));
    }

    @Test
    void shouldSuccessfulPlanAndReplanMeetingUsingVidgets() {
        var daysToAddForFirstMeeting = 3;
        var firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        String firstMeetingDay;
        if (firstMeetingDate.charAt(0) == '0') {
            firstMeetingDay = firstMeetingDate.substring(1, 2);
        } else {
            firstMeetingDay = firstMeetingDate.substring(0, 2);
        }
        var daysToAddForSecondMeeting = 12;
        var secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);
        String secondMeetingDay;
        if (secondMeetingDate.charAt(0) == '0') {
            secondMeetingDay = secondMeetingDate.substring(1, 2);
        } else {
            secondMeetingDay = secondMeetingDate.substring(0, 2);
        }
        var locale = "ru";
        var city = DataGenerator.generateCity();
        $("[data-test-id=city] input").val(city.substring(0, 2));
        $(withText(city)).scrollTo().click();
        $("button .icon_name_calendar").click();
        if (firstMeetingDay.length() == 1) {
            $$("[data-step=1]").filterBy(visible).first().click();
            $$("[data-day]").filterBy(visible).findBy(exactText(firstMeetingDay)).click();
        } else {
            $$("[data-day]").filterBy(visible).findBy(exactText(firstMeetingDay)).click();
        }
        $("[data-test-id=name] input").val(DataGenerator.generateName(locale));
        $("[data-test-id=phone] input").val(DataGenerator.generatePhone(locale));
        $("[data-test-id=agreement]").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=success-notification]").shouldBe(visible, Duration.ofSeconds(15));
        $(".notification__content").shouldHave(text("Встреча успешно запланирована на " + firstMeetingDate))
                .shouldBe(visible, Duration.ofSeconds(15));
        $("button .icon_name_calendar").click();
        if (secondMeetingDay.length() == 1) {
            $$("[data-step='1']").filterBy(visible).first().click();
            $$("[data-day]").filterBy(visible).findBy(exactText(secondMeetingDay)).click();
        } else {
            $$("[data-day]").filterBy(visible).findBy(exactText(secondMeetingDay)).click();
        }
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=replan-notification]").shouldBe(visible, Duration.ofSeconds(15));
        $$(".notification__content")
                .findBy(text("У вас уже запланирована встреча на другую дату. Перепланировать?"))
                .shouldBe(visible, Duration.ofSeconds(15));
        $$("button").find(exactText("Перепланировать")).click();
        $("[data-test-id=success-notification]").shouldBe(visible, Duration.ofSeconds(15));
        $(".notification__content").shouldHave(text("Встреча успешно запланирована на " + secondMeetingDate))
                .shouldBe(visible, Duration.ofSeconds(15));
    }

    @Test
    void shouldNotPlanMeetingForInvalidCity() {
        $("[data-test-id=city] input").val("Приозерск");
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").val(DataGenerator.generateDate(3));
        $("[data-test-id=name] input").val(DataGenerator.generateName("ru"));
        $("[data-test-id=phone] input").val(DataGenerator.generatePhone("ru"));
        $("[data-test-id=agreement]").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=city].input_invalid .input__sub")
                .shouldBe(exactText("Доставка в выбранный город недоступна"));
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
    }

    @Test
    void shouldNotPlanMeetingForInvalidDate() {
        String invalidDate = DataGenerator.generateDate(2);
        $("[data-test-id=city] input").val(DataGenerator.generateCity());
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").val(invalidDate);
        $("[data-test-id=name] input").val(DataGenerator.generateName("ru"));
        $("[data-test-id=phone] input").val(DataGenerator.generatePhone("ru"));
        $("[data-test-id=agreement]").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=date] .input__sub")
                .shouldBe(exactText("Заказ на выбранную дату невозможен"));
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
    }

    @Test
    void shouldNotPlanMeetingForInvalidName() {
        $("[data-test-id=city] input").val(DataGenerator.generateCity());
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").val(DataGenerator.generateDate(3));
        $("[data-test-id=name] input").val(DataGenerator.generateName("en"));
        $("[data-test-id=phone] input").val(DataGenerator.generatePhone("ru"));
        $("[data-test-id=agreement]").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=name].input_invalid .input__sub")
                .shouldBe(exactText("Имя и Фамилия указаные неверно. " +
                        "Допустимы только русские буквы, пробелы и дефисы."));
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
    }

//    @Test
//    void shouldNotPlanMeetingForInvalidPhone() {
//        $("[data-test-id=city] input").val(DataGenerator.generateCity());
//        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
//        $("[data-test-id=date] input").val(DataGenerator.generateDate(4));
//        $("[data-test-id=name] input").val(DataGenerator.generateName("ru"));
//        $("[data-test-id=phone] input").val(DataGenerator.generatePhone("ru").substring(0,9));
//        $("[data-test-id=agreement]").click();
//        $$("button").find(exactText("Запланировать")).click();
//        $("[data-test-id=phone].input_invalid .input__sub")
//                .shouldBe(exactText("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678."));
//        $$("button").find(exactText("Запланировать")).click();
//        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
//
//    }

    @Test
    void shouldNotPlanMeetingForEmptyFields() {
        $("[data-test-id=agreement]").click();
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=city].input_invalid .input__sub")
                .shouldBe(exactText("Поле обязательно для заполнения"));
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
        $("[data-test-id=city] input").val(DataGenerator.generateCity());
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=date] .input__sub")
                .shouldBe(exactText("Неверно введена дата"));
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
        $("[data-test-id=date] input").val(DataGenerator.generateDate(4));
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=name].input_invalid .input__sub")
                .shouldBe(exactText("Поле обязательно для заполнения"));
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
        $("[data-test-id=name] input").val(DataGenerator.generateName("ru"));
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=phone].input_invalid .input__sub")
                .shouldBe(exactText("Поле обязательно для заполнения"));
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
    }

    @Test
    void shouldNotPlanMeetingForCheckBoxNotChecked() {
        $("[data-test-id=city] input").val(DataGenerator.generateCity());
        $("[data-test-id=date] input").sendKeys(Keys.chord(Keys.SHIFT, Keys.HOME), Keys.BACK_SPACE);
        $("[data-test-id=date] input").val(DataGenerator.generateDate(7));
        $("[data-test-id=name] input").val(DataGenerator.generateName("ru"));
        $("[data-test-id=phone] input").val(DataGenerator.generatePhone("ru"));
        $$("button").find(exactText("Запланировать")).click();
        $("[data-test-id=agreement].input_invalid")
                .shouldBe(exactText("Я соглашаюсь с условиями обработки и использования моих персональных данных"));
        $("[data-test-id=success-notification]").shouldNotBe(visible, Duration.ofSeconds(15));
    }
}