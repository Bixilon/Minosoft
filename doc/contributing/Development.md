# Development
Generally I'd say TBA, but some basic rules are here. Feel free to contact us first, before developing huge things. May be better.

## Git Rules
- Small Features in `development`.
- `master` must always be stable and functional.
- No force pushes (or modifying the git history) in protected branches (aka. `master`).
- Bigger features on own branches. Merge only via Merge Request.
- No commits directly in `master`

## Git access

- Don't just request developer access, I'll not just give you access, instead create a fork, and a pull requests, we will accept (or decline) it. Probably a bit of discussion.
- If you develop here for a while, you can request developer access, and I'll contact it. But probably you won't need it, but I'll probably don't refuse to give it to you.

## Pull Requests

- You can either create a pull request on my gitlab (easiest)
- Use another git and make a pull request there (Consider looking here: [Gitlab-14116](https://gitlab.com/gitlab-org/gitlab/-/issues/14116))
- Send me patch files per email

## Where to start

- Issues (just browse through them and pick an interesting one)
- ToDos: In the code are many todos. Just search for them and resolve them.
- Interesting features you'd like to have included

## What not to include

- This is a non-profit project, so don't include anything that violates open source rules.
- No connections to other servers than mojang or resources (aka this or another git repo).
- No remote code execution or similar (You know what I mean).
- No copyright protected content.
- Server specific support (Consider writing a [mod](/doc/Modding.md)).

## How to start (small features)

1. Clone the repository
2. Implement a feature (feel free to ask if you have questions)
3. Test and check for impacts
4. Submit **P**ull **R**equest (target branch should be `master`)
5. Wait for merge

## How to start (big features)

1. Clone the repository
2. Start implementing features
3. Once you have something (minimal) to show, open a draft merge request (Start title with `WIP`) and write (short or long) what you have and want to implement (consider looking at other PRs)
3. Test fix all bugs (or discuss about them)
4. Let other people test
6. Mark PR as ready
7. Wait for merge
