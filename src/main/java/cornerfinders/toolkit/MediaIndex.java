package cornerfinders.toolkit;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;

/**
 * Keeps track of and can look up relevant information about media files 
 * used in MultimodalActionHistory. The MediaInfo objects should have unique
 * ids and unique filenames
 * @author cadlerun
 *
 */
public class MediaIndex {

  private HashMap<String, MediaInfo> m_mediaIdMap; // maps ids to MediaInfo objects
  private HashMap<String, MediaInfo> m_mediaFilenameMap; // maps filenames to MediaInfo objects
  
  private HashMap<String, Clip> m_clipMap; // maps ids to clips
  
  private File m_directory = null; // directory to look for files in
  
  public MediaIndex() {
    m_mediaIdMap = new HashMap<String, MediaInfo>();
    m_mediaFilenameMap = new HashMap<String, MediaInfo>();
    m_clipMap = new HashMap<String, Clip>();
  }
  
  /**
   * Add a MediaInfo to the MediaIndex
   * @param mediaInfo The MediaInfo to add.
   */
  public void add(MediaInfo mediaInfo) {
    m_mediaIdMap.put(mediaInfo.getId(), mediaInfo);
    m_mediaFilenameMap.put(mediaInfo.getFilename(), mediaInfo);
  }
  
  /**
   * Remove a MediaInfo from the MediaIndex
   * @param mediaInfo The MediaInfo to remove.
   */
  public void remove(MediaInfo mediaInfo) {
    m_mediaIdMap.remove(mediaInfo.getId());
    m_mediaFilenameMap.remove(mediaInfo.getFilename());
  }
  
  /**
   * Determine if there is MediaInfo with the specified filename in the MediaIndex
   * @param filename the filename of the MediaInfo to look for
   * @return true if there is a MediaInfo with the specified filename in the MediaIndex, false otherwise
   */
  public boolean hasMediaInfoByFilename(String filename) {
    return m_mediaFilenameMap.containsKey(filename);
  }
  
  /**
   * Get the MediaInfo object with the specified filename
   * @param filename the filename to look up the MediaInfo object with 
   * @return the MediaInfo object with the specified filename or null if it doesn't exist
   */
  public MediaInfo getMediaInfoByFilename(String filename) {
    return m_mediaFilenameMap.get(filename);
  }
  
  /**
   * Determine if there is MediaInfo with the specified id in the MediaIndex
   * @param id the id of the MediaInfo to look for
   * @return true if there is a MediaInfo with the specified id in the MediaIndex, false otherwise
   */
  public boolean hasMediaInfoById(String id) {
    return m_mediaIdMap.containsKey(id);
  }
  
  /**
   * Get the MediaInfo object with the specified id
   * @param id the id to look up the MediaInfo object with 
   * @return the MediaInfo object with the specified id or null if it doesn't exist
   */
  public MediaInfo getMediaInfoById(String id) {
    return m_mediaIdMap.get(id);
  }
  
  /**
   * Get a Collection of all MediaInfo objects in the MediaIndex
   * @return a Collection of all the MediaInfo objects in the MediaIndex
   */
  public Collection<MediaInfo> getMedia() {
    return m_mediaIdMap.values();  
  }

  /**
   * Get or create an audio Clip for the specified MediaInfo object
   * @param media the MediaInfo to get a Clip for
   * @return a Clip for the specified MediaInfo
   */
  public Clip getAudioClip(MediaInfo media) {
    if (m_clipMap.containsKey(media.getId()))
      return m_clipMap.get(media.getId());
    
    // need to create a new Clip
    Clip clip = null;
    File f = new File(m_directory, media.getFilename());
    try {
      AudioInputStream ain = AudioSystem.getAudioInputStream(f);
      try {
        DataLine.Info info = new DataLine.Info(Clip.class, ain.getFormat());
        clip = (Clip) AudioSystem.getLine(info);
        clip.open(ain);
      }
      finally {
        ain.close();
      }
    }
    catch (Exception io) {
      return null;
    }
    
    m_clipMap.put(media.getId(), clip);
    return clip;
  }

  /**
   * Set the directory to look for files in
   * @param directory the directory to look for files in
   */
  public void setDirectory(File directory) {
    m_directory = directory;
  }
  
}
