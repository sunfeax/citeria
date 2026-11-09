# Development seed data

These migrations run only with the `dev` profile, alongside the main schema migrations.

`V20261109180000__add_specialist_avatars.sql` adds JPEG portraits for the fictional Anna and Boris accounts. The images are embedded as base64 so seeding works offline and through the existing avatar API. Existing avatars are preserved.

Portraits are cropped to 256 × 256 pixels and compressed as JPEG:

- Anna: [Woman in Collared Shirt — Andrea Piacquadio, Pexels](https://www.pexels.com/photo/woman-in-collared-shirt-774909/).
- Boris: [Man Wearing Black T-shirt — Stefan Stefancik, Pexels](https://www.pexels.com/photo/man-wearing-black-t-shirt-220453/).

[Pexels license](https://www.pexels.com/license/). The people pictured are stock models, not actual Citeria specialists.

`V20261109190000__add_remaining_demo_specialists.sql` persists six additional fictional specialists (David, Elena, Laura, Marcus, Sofia and Tomas), their 16 existing demo offerings, and portraits. It preserves existing users, offerings and avatars. Together, both avatar seeds cover all eight demo specialists.

Additional Pexels portraits, cropped to 256 × 256 JPEG:

- David: [Pexels photo 614810](https://www.pexels.com/photo/614810/).
- Elena: [Pexels photo 415829](https://www.pexels.com/photo/415829/).
- Laura: [Pexels photo 1239291](https://www.pexels.com/photo/1239291/).
- Marcus: [Pexels photo 2379004](https://www.pexels.com/photo/2379004/).
- Sofia: [Pexels photo 733872](https://www.pexels.com/photo/733872/).
- Tomas: [Pexels photo 91227](https://www.pexels.com/photo/91227/).
