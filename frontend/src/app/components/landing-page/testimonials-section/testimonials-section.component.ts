import { AfterViewInit, Component } from '@angular/core';
import { TranslateModule, TranslateService } from '@ngx-translate/core';

@Component({
  selector: 'app-testimonials-section',
  standalone: true,
  imports: [TranslateModule],
  templateUrl: './testimonials-section.component.html',
  styleUrls: ['./testimonials-section.component.css'],
})
export class TestimonialsSectionComponent implements AfterViewInit {
  ngAfterViewInit(): void {
    // This code handles the mouse and touch events for a horizontal scrolling gallery.
    // It allows users to click and drag to scroll through the gallery of testimonials.
    // It also supports touch events for mobile devices.
    const track = document.querySelector('.gallery') as HTMLElement;

    const handleOnDown = (e: MouseEvent | TouchEvent) => {
      if (e instanceof MouseEvent) {
        track.dataset['mouseDownAt'] = e.clientX.toString();
      } else {
        track.dataset['mouseDownAt'] = e.touches[0].clientX.toString();
      }
      // Block vertical scrolling while dragging
      document.body.style.overflowY = 'hidden';
      document.body.style.touchAction = 'pan-x';
    };

    const handleOnUp = () => {
      track.dataset['mouseDownAt'] = '0';
      track.dataset['prevPercentage'] = track.dataset['percentage'];
      // Restore vertical scrolling when drag ends
      document.body.style.overflowY = '';
      document.body.style.touchAction = '';
    };

    const handleOnMove = (e: MouseEvent | TouchEvent) => {
      if (track.dataset['mouseDownAt'] === '0') return;
      if (track.clientWidth === 0) return; // Prevent errors if track is not visible

      const clientX =
        e instanceof MouseEvent ? e.clientX : e.touches[0].clientX;

      const mouseDownAt = track.dataset['mouseDownAt']
        ? parseFloat(track.dataset['mouseDownAt'])
        : 0;

      const mouseDelta = mouseDownAt - clientX;

      const maxDelta = window.innerWidth / 2;

      const percentage = (mouseDelta / maxDelta) * -100;

      const prevPercentage = track.dataset['prevPercentage']
        ? parseFloat(track.dataset['prevPercentage'])
        : 0;

      const nextPercentageUnconstrained = prevPercentage + percentage;

      const maxScrollPercentage =
        (-(track.scrollWidth - track.clientWidth) / track.clientWidth) * 100;

      const nextPercentage = Math.max(
        Math.min(nextPercentageUnconstrained, 0),
        maxScrollPercentage
      );

      track.dataset['percentage'] = nextPercentage.toString();
      track.animate(
        {
          transform: `translate(${nextPercentage}%, 0%)`,
        },
        { duration: 1200, fill: 'forwards' }
      );
      for (const image of Array.from(
        track.querySelectorAll('.testimonial-gallery-img')
      )) {
        const objectPositionX = Math.max(0, Math.min(100, 100 + nextPercentage / 3));
        image.animate(
          {
            objectPosition: `${objectPositionX}% center`,
          },
          { duration: 1200, fill: 'forwards' }
        );
      }
    };
    window.onmousedown = (e) => handleOnDown(e);
    window.ontouchstart = (e) => handleOnDown(e);
    window.onmouseup = () => handleOnUp();
    window.ontouchend = () => handleOnUp();
    window.onmousemove = (e) => handleOnMove(e);
    window.ontouchmove = (e) => handleOnMove(e);
  }

  constructor(private translate: TranslateService) {
    this.translate.setDefaultLang('en');
  }

  changeLanguage(language: string): void {
    this.translate.use(language);
  }
}
