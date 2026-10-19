# Technician Navigation — Hazeline branch

This bundle contains the technician dashboard navigation files extracted from the uploaded PeAirbrakes2 Android project, with explanatory comments added.

## Files
- `app/src/main/java/com/example/peairbrakes/TechnicianDashboardActivity.kt` — click/navigation logic for the technician dashboard.
- `app/src/main/res/layout/activity_technician_dashboard.xml` — dashboard screen layout and the four navigation cards.

## What the code currently does
- **Active Breakdowns** opens `ActiveBreakdownsActivity`.
- **Diesel Slips** opens `DieselSlipsActivity`.
- **Completed Jobs** displays a Toast message only (placeholder).
- **Log Breakdown** displays a Toast message only (placeholder).

The two target activities and referenced resources must already exist in the repository for the project to build. The uploaded project manifest already declares the technician dashboard and the two navigated-to activities.

## Suggested commit message
`Add technician dashboard navigation and comments`

## Push to the existing `Hazeline` branch
Use Git locally from the existing project folder (recommended, because it preserves the project structure):

```bash
git status
git switch Hazeline
# Copy the two files from this bundle into the matching paths in your project.
git add app/src/main/java/com/example/peairbrakes/TechnicianDashboardActivity.kt app/src/main/res/layout/activity_technician_dashboard.xml
git commit -m "Add technician dashboard navigation and comments"
git push origin Hazeline
```

If your local project folder is not connected to the GitHub repository yet, open the repository folder in a terminal and check `git remote -v` before pushing. Do not run `git init` or overwrite your repository if it is already set up.

## GitHub website upload alternative
1. Open your repository on GitHub and select the `Hazeline` branch.
2. Use **Add file → Upload files**.
3. Upload each file while preserving its folder path. If GitHub's upload interface does not preserve nested folders, create/open each folder and add the file at its exact path.
4. Enter the commit message above and commit directly to `Hazeline`.

For evidence of your contribution, take screenshots showing the branch selector set to `Hazeline`, the changed files, and the commit details. Only claim the files/actions that you actually committed.
