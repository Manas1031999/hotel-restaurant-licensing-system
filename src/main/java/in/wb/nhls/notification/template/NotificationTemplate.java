package in.wb.nhls.notification.template;

import java.util.Map;

// {key} জায়গাগুলো params থেকে বসে
public enum NotificationTemplate {

    APPLICATION_SUBMITTED(
            "আবেদন জমা হয়েছে: {appNo}",
            "প্রিয় {ownerName},\n\n{businessName}-এর লাইসেন্স আবেদন জমা হয়েছে।\n"
                    + "Application Number: {appNo}\n\nআবেদনের অবস্থা বদলালে আপনাকে জানানো হবে।"),

    APPLICATION_STATUS_CHANGED(
            "আবেদনের অবস্থা বদলেছে: {appNo} এখন {status}",
            "প্রিয় {ownerName},\n\n{businessName}-এর আবেদন ({appNo}) এখন {status} অবস্থায় আছে।\n"
                    + "মন্তব্য: {remarks}"),

    LICENSE_ISSUED(
            "লাইসেন্স জারি হয়েছে: {licenseNo}",
            "প্রিয় {ownerName},\n\n{businessName}-এর লাইসেন্স জারি হয়েছে।\n"
                    + "License Number: {licenseNo}\nমেয়াদ: {validUntil} পর্যন্ত"),

    LICENSE_RENEWED(
            "লাইসেন্স নবীকরণ হয়েছে: {licenseNo}",
            "প্রিয় {ownerName},\n\n{businessName}-এর লাইসেন্স নবীকরণ হয়েছে।\n"
                    + "নতুন মেয়াদ: {validUntil} পর্যন্ত"),

    LICENSE_EXPIRED(
            "লাইসেন্সের মেয়াদ শেষ: {licenseNo}",
            "প্রিয় {ownerName},\n\n{businessName}-এর লাইসেন্স ({licenseNo})-এর মেয়াদ শেষ হয়ে গেছে।\n"
                    + "ব্যবসা চালু রাখতে অনুগ্রহ করে দ্রুত নবীকরণ করুন।");

    private final String subject;
    private final String body;

    NotificationTemplate(String subject, String body) {
        this.subject = subject;
        this.body = body;
    }

    public String renderSubject(Map<String, ?> params) {
        return render(subject, params);
    }

    public String renderBody(Map<String, ?> params) {
        return render(body, params);
    }

    private static String render(String text, Map<String, ?> params) {
        String result = text;
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            Object value = entry.getValue();
            result = result.replace("{" + entry.getKey() + "}", value == null ? "-" : value.toString());
        }
        return result;
    }
}
