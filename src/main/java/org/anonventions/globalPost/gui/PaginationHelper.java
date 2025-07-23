package org.anonventions.globalPost.gui;

import org.anonventions.globalPost.models.Mail;

import java.util.List;

/**
 * Helper class for managing pagination in mail GUIs
 */
public class PaginationHelper {
    
    private final List<Mail> allMails;
    private final int itemsPerPage;
    private int currentPage;
    
    public PaginationHelper(List<Mail> mails, int itemsPerPage) {
        this.allMails = mails;
        this.itemsPerPage = itemsPerPage;
        this.currentPage = 0;
    }
    
    /**
     * Get mails for the current page
     */
    public List<Mail> getCurrentPageMails() {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allMails.size());
        
        if (startIndex >= allMails.size()) {
            return List.of();
        }
        
        return allMails.subList(startIndex, endIndex);
    }
    
    /**
     * Check if there's a next page
     */
    public boolean hasNextPage() {
        return (currentPage + 1) * itemsPerPage < allMails.size();
    }
    
    /**
     * Check if there's a previous page
     */
    public boolean hasPreviousPage() {
        return currentPage > 0;
    }
    
    /**
     * Go to next page
     */
    public void nextPage() {
        if (hasNextPage()) {
            currentPage++;
        }
    }
    
    /**
     * Go to previous page
     */
    public void previousPage() {
        if (hasPreviousPage()) {
            currentPage--;
        }
    }
    
    /**
     * Get current page number (0-based)
     */
    public int getCurrentPage() {
        return currentPage;
    }
    
    /**
     * Get total number of pages
     */
    public int getTotalPages() {
        return (int) Math.ceil((double) allMails.size() / itemsPerPage);
    }
    
    /**
     * Get total number of mails
     */
    public int getTotalMails() {
        return allMails.size();
    }
    
    /**
     * Reset to first page
     */
    public void resetToFirstPage() {
        currentPage = 0;
    }
}