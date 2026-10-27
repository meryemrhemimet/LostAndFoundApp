package com.example.myapplication.model;

// Modèle représentant un objet perdu ou trouvé
public class LostItem {
    private long id;
    private String title, description, address, category, type, status;
    private String photoPath, phone, userName, userEmail, userId;
    private double latitude, longitude;
    private long createdAt;

    public LostItem() { this.status = "active"; this.createdAt = System.currentTimeMillis(); }

    public LostItem(long id, String userId, String title, String description, String address) {
        this(); this.id = id; this.userId = userId; this.title = title;
        this.description = description; this.address = address;
    }

    // Getters & Setters
    public long getId() { return id; } public void setId(long id) { this.id = id; }
    public String getTitle() { return title; } public void setTitle(String t) { this.title = t; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public String getAddress() { return address; } public void setAddress(String a) { this.address = a; }
    public String getCategory() { return category; } public void setCategory(String c) { this.category = c; }
    public String getType() { return type; } public void setType(String t) { this.type = t; }
    public String getStatus() { return status; } public void setStatus(String s) { this.status = s; }
    public String getPhotoPath() { return photoPath; } public void setPhotoPath(String p) { this.photoPath = p; }
    public String getPhone() { return phone; } public void setPhone(String p) { this.phone = p; }
    public String getUserName() { return userName; } public void setUserName(String u) { this.userName = u; }
    public String getUserEmail() { return userEmail; } public void setUserEmail(String e) { this.userEmail = e; }
    public String getUserId() { return userId; } public void setUserId(String u) { this.userId = u; }
    public double getLatitude() { return latitude; } public void setLatitude(double l) { this.latitude = l; }
    public double getLongitude() { return longitude; } public void setLongitude(double l) { this.longitude = l; }
    public long getCreatedAt() { return createdAt; } public void setCreatedAt(long c) { this.createdAt = c; }
}
