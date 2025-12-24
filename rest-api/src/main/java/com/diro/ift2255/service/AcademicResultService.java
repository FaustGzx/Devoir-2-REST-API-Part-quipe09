package com.diro.ift2255.service;

import com.diro.ift2255.model.AcademicResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
/**
 * Service responsable du chargement et de la consultation des résultats académiques agrégés
 * (données CSV) pour les cours.
 *
 * <p>Les résultats sont chargés au démarrage depuis une ressource (classpath)
 * et indexés par sigle de cours (ex : IFT2255).</p>
 */

public class AcademicResultService {
    /** Index des résultats académiques agrégés, par sigle de cours (en majuscules). */
    private final Map<String, AcademicResult> bySigle = new HashMap<>();
    /**
    * Construit le service et charge les résultats académiques agrégés depuis une ressource CSV.
    *
    * @param resourceName nom/chemin de la ressource CSV sur le classpath (ex : "results.csv")
    */
    public AcademicResultService(String resourceName) {
        loadFromResource(resourceName);
    }

    private void loadFromResource(String resourceName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            if (is == null) {
                System.err.println("[AcademicResultService] CSV introuvable: " + resourceName);
                return;
            }

            int loaded = 0;
            int skipped = 0;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                br.readLine(); // header

                String line;
                while ((line = br.readLine()) != null) {
                    List<String> parts = parseCsvLine(line);
                    if (parts.size() < 6) { skipped++; continue; }

                    try {
                        String sigle = parts.get(0).trim().toUpperCase();
                        String nom = parts.get(1).trim();
                        String moyenne = parts.get(2).trim();
                        double score = Double.parseDouble(parts.get(3).trim());
                        int participants = Integer.parseInt(parts.get(4).trim());
                        int trimestres = Integer.parseInt(parts.get(5).trim());

                        bySigle.put(sigle, new AcademicResult(sigle, nom, moyenne, score, participants, trimestres));
                        loaded++;
                    } catch (Exception e) {
                        skipped++;
                    }
                }
            }

            System.out.println("[AcademicResultService] Chargé=" + loaded + " | Ignoré=" + skipped);

        } catch (Exception e) {
            System.err.println("[AcademicResultService] Erreur chargement CSV: " + e.getMessage());
        }
    }

    // Parser CSV simple avec guillemets (gère les virgules dans les champs)
    private List<String> parseCsvLine(String line) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                // double quote escape "" -> "
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    cur.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out;
    }
    /**
    * Retourne les résultats académiques agrégés pour un cours donné.
    *
    * @param sigle sigle du cours (ex : "IFT2255"), non sensible à la casse
    * @return un {@link Optional} contenant les résultats si trouvés, sinon vide
    */
    public Optional<AcademicResult> getBySigle(String sigle) {
        if (sigle == null) return Optional.empty();
        return Optional.ofNullable(bySigle.get(sigle.trim().toUpperCase()));
    }
}
