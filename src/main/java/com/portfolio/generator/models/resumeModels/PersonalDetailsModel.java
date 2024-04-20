package com.portfolio.generator.models.resumeModels;

import java.util.List;

public class PersonalDetailsModel {
  private String firstName;
  private String lastName;
  private String positionTitle;
  private String location;
  private String tagline;
  private String avatar;
  private String email;
  private String phone;
  private String website;
  private String linkedin;
  private String github;
  private List<LanguageModel> languages;

  public PersonalDetailsModel() {
  }

  public PersonalDetailsModel(
      String firstName,
      String lastName,
      String positionTitle,
      String location,
      String tagline,
      String avatar,
      String email,
      String phone,
      String website,
      String linkedin,
      String github,
      List<LanguageModel> languages
  ) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.positionTitle = positionTitle;
    this.location = location;
    this.tagline = tagline;
    this.avatar = avatar;
    this.email = email;
    this.phone = phone;
    this.website = website;
    this.linkedin = linkedin;
    this.github = github;
    this.languages = languages;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPositionTitle() {
    return positionTitle;
  }

  public void setPositionTitle(String positionTitle) {
    this.positionTitle = positionTitle;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getTagline() {
    return tagline;
  }

  public void setTagline(String tagline) {
    this.tagline = tagline;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getWebsite() {
    return website;
  }

  public void setWebsite(String website) {
    this.website = website;
  }

  public String getLinkedin() {
    return linkedin;
  }

  public void setLinkedin(String linkedin) {
    this.linkedin = linkedin;
  }

  public String getGithub() {
    return github;
  }

  public void setGithub(String github) {
    this.github = github;
  }

  public List<LanguageModel> getLanguages() {
    return languages;
  }

  public void setLanguages(List<LanguageModel> languages) {
    this.languages = languages;
  }
}
