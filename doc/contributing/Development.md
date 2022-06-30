# Development
Generally I'd say TBA, but some basic rules are here. Feel free to contact me first, before developing huge things. May be better.

## Git Rules
- `master` must always be stable and functional.
- No force pushes (or modifying the git history) in protected branches (aka. `master`).
- Bigger features on own branches. Merge only via Merge Request.

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

- This is a non-profit project, so don't include anything that makes money or includes closed source code.
- No connections to other servers than mojang or resources (aka this or another git repo).
- No ads, no data collection, nothing in this direction.
- No copyright protected content.
- Server specific support (Consider writing a [mod](/doc/Modding.md)).

## How to start

1. Fork the repository
2. Clone your fork
3. Create a branch and start developing there
4. Implement a feature (feel free to ask if you have questions)
5. Test and check for impacts
6. Submit **P**ull **R**equest (target branch should be `master`)
7. Wait for merge
